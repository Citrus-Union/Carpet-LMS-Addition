/*
 * Copyright (C) 2025  Carpet-LMS-Addition contributors
 * https://github.com/Citrus-Union/Carpet-LMS-Addition

 * Carpet LMS Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.

 * Carpet LMS Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Carpet LMS Addition.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.nm.lms.carpetlmsaddition.rule.util.storage.website;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import cn.nm.lms.carpetlmsaddition.Mod;
import cn.nm.lms.carpetlmsaddition.lib.AsyncFileIo;
import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;
import cn.nm.lms.carpetlmsaddition.lib.getvalue.GetPaths;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.util.storage.GenerateResponse;
import cn.nm.lms.carpetlmsaddition.rule.util.storage.Storage;
import cn.nm.lms.carpetlmsaddition.safety.Password;

public class Website {
    private static final Gson GSON = new Gson();
    private static final Path CONFIG_JSON_PATH = GetPaths.getLmsWorldPath().resolve("checkStorageConfig.json");
    private static final Path SECRET_PATH = GetPaths.getLmsConfigSecretPath().resolve("storage");
    private static final Path CUSTOM_STORAGE_WEBSITE_PATH = GetPaths.getLmsWorldPath().resolve("customStorageWebsite");
    private static final String CUSTOM_STORAGE_INDEX_HTML = "This storage viewer is to be customized.<br>这是待修改的仓储查看";
    private static HttpServer app;
    private static int autoUpdateDataTicks = 0;
    private static int lastAutoUpdateInterval = 0;

    public static boolean isServerRunning() {
        return app != null;
    }

    public static void startServer() {
        if (app != null) {
            return;
        }

        int port = getPort();
        try {
            app = HttpServer.create(new InetSocketAddress(port), 0);
            app.setExecutor(AsyncTasks.executor());
            app.createContext("/", Website::handle);
            app.start();
            resetAutoUpdateDataState();
            Mod.LOGGER.info("Started Storage Website Server in port {}", port);
        } catch (IOException e) {
            app = null;
            Mod.LOGGER.warn("Failed to start Storage Website Server on port {}", port);
        }
    }

    public static void stopServer() {
        if (app != null) {
            app.stop(0);
            app = null;
            resetAutoUpdateDataState();
            Mod.LOGGER.info("Stopped Storage Website Server");
        }
    }

    public static void tickAutoUpdateData() {
        if (!isServerRunning()) {
            resetAutoUpdateDataState();
            return;
        }

        int interval = Settings.checkStorageAutoUpdateDataInterval;
        if (interval <= 0) {
            resetAutoUpdateDataState();
            return;
        }
        if (interval != lastAutoUpdateInterval) {
            autoUpdateDataTicks = 0;
            lastAutoUpdateInterval = interval;
        }

        autoUpdateDataTicks++;
        if (autoUpdateDataTicks < interval) {
            return;
        }

        autoUpdateDataTicks = 0;
        try {
            Storage.checkStorage();
        } catch (Exception e) {
            Mod.LOGGER.warn("Failed to auto update checkStorage data", e);
        }
    }

    public static void autoStartFromConfigAfterWorldLoaded() {
        if (isAutoStartWebsiteEnabled()) {
            startServer();
        }
    }

    private static void resetAutoUpdateDataState() {
        autoUpdateDataTicks = 0;
        lastAutoUpdateInterval = 0;
    }

    private static void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        boolean noPasswordEnabled = isNoPasswordEnabled();

        if ("/api/storage/getData".equals(path)) {
            if (!"GET".equals(method)) {
                writeJson(exchange, 405, new WebsiteMessageResp("405", "Method Not Allowed"));
                return;
            }
            WebsiteAuth.AuthResult auth = WebsiteAuth.resolve(exchange.getRequestHeaders().getFirst("Authorization"),
                noPasswordEnabled, SECRET_PATH);
            if (auth.errorMessage != null) {
                writeJson(exchange, 401, new WebsiteMessageResp("401", auth.errorMessage));
                return;
            }
            handleGetDataAsync(exchange);
            return;
        }

        if ("/api/storage/getItem".equals(path)) {
            if (!"POST".equals(method)) {
                writeJson(exchange, 405, new WebsiteMessageResp("405", "Method Not Allowed"));
                return;
            }
            if (!Settings.websiteGetItem) {
                writeJson(exchange, 403, new WebsiteMessageResp("403", "Website getItem is disabled"));
                return;
            }
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            handleWebsiteGetItemAsync(exchange, authHeader, body, noPasswordEnabled);
            return;
        }

        if ("/api/login".equals(path)) {
            if (!"POST".equals(method)) {
                writeJson(exchange, 405, new WebsiteMessageResp("405", "Method Not Allowed"));
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            handleLoginAsync(exchange, body);
            return;
        }

        if (!"GET".equals(method)) {
            write(exchange, 405, "text/plain; charset=utf-8", "Method Not Allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }

        writeWebsiteStatic(exchange, path);
    }

    private static void writeWebsiteStatic(HttpExchange exchange, String path) throws IOException {
        String file = "/".equals(path) ? "/index.html" : path;
        if (file.contains("..")) {
            write(exchange, 400, "text/plain; charset=utf-8", "Bad Request".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String resolvedFile = file;
        byte[] content;
        if (isCustomWebsiteEnabled()) {
            if (!ensureCustomWebsiteReady()) {
                write(exchange, 404, "text/plain; charset=utf-8", "Not Found".getBytes(StandardCharsets.UTF_8));
                return;
            }
            content = readCustomResource(resolveCustomResourcePath(file));
            if (content == null && !file.contains(".")) {
                resolvedFile = "/index.html";
                content = readCustomResource(resolveCustomResourcePath(resolvedFile));
            }
            if (content == null) {
                write(exchange, 404, "text/plain; charset=utf-8", "Not Found".getBytes(StandardCharsets.UTF_8));
                return;
            }
        } else {
            String resource = "websites/storage" + file;
            content = readBundledResource(resource);
            if (content == null && !file.contains(".")) {
                resolvedFile = "/index.html";
                resource = "websites/storage/index.html";
                content = readBundledResource(resource);
            }
            if (content == null) {
                write(exchange, 404, "text/plain; charset=utf-8", "Not Found".getBytes(StandardCharsets.UTF_8));
                return;
            }
        }

        write(exchange, 200, getContentType(resolvedFile), content);
    }

    private static String getContentType(String file) {
        if (file.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (file.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (file.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (file.endsWith(".json")) {
            return "application/json; charset=utf-8";
        }
        return "application/octet-stream";
    }

    private static boolean ensureCustomWebsiteReady() {
        try {
            AsyncFileIo.createDirectories(CUSTOM_STORAGE_WEBSITE_PATH);
            Path customIndexHtmlPath = CUSTOM_STORAGE_WEBSITE_PATH.resolve("index.html");
            if (!AsyncFileIo.exists(customIndexHtmlPath)) {
                AsyncFileIo.writeString(customIndexHtmlPath, CUSTOM_STORAGE_INDEX_HTML);
            }
            return true;
        } catch (IOException e) {
            Mod.LOGGER.warn("Failed to prepare custom storage website directory: {}", CUSTOM_STORAGE_WEBSITE_PATH, e);
            return false;
        }
    }

    private static Path resolveCustomResourcePath(String file) {
        String normalizedFile = file.startsWith("/") ? file.substring(1) : file;
        Path root = CUSTOM_STORAGE_WEBSITE_PATH.normalize();
        Path resolved = root.resolve(normalizedFile).normalize();
        if (!resolved.startsWith(root)) {
            return null;
        }
        return resolved;
    }

    private static byte[] readCustomResource(Path filePath) {
        if (filePath == null || !AsyncFileIo.exists(filePath)) {
            return null;
        }
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            Mod.LOGGER.warn("Failed to read custom website file: {}", filePath, e);
            return null;
        }
    }

    private static byte[] readBundledResource(String resource) {
        try {
            Path resourcePath = Mod.getModContainer().findPath(resource).orElse(null);
            return resourcePath == null ? null : Files.readAllBytes(resourcePath);
        } catch (IOException e) {
            Mod.LOGGER.warn("Failed to read website resource: {}", resource, e);
            return null;
        }
    }

    private static boolean isCustomWebsiteEnabled() {
        try {
            JsonElement customWebsite =
                JsonParser.parseString(AsyncFileIo.readString(CONFIG_JSON_PATH)).getAsJsonObject().get("customWebsite");
            if (customWebsite != null && customWebsite.isJsonPrimitive()
                && customWebsite.getAsJsonPrimitive().isBoolean()) {
                return customWebsite.getAsBoolean();
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static void handleLoginAsync(HttpExchange exchange, String body) {
        CompletableFuture<Password.Result> authFuture =
            Password.LoginRequest.authenticateAsync(body, SECRET_PATH, getExpireDay());
        authFuture.whenComplete((result, throwable) -> {
            try {
                if (throwable != null) {
                    Mod.LOGGER.warn("Unknown error while authenticating", throwable);
                    writeJson(exchange, 500, new WebsiteMessageResp("500", "Unknown error"));
                    return;
                }
                if (!result.isSuccess()) {
                    writeJson(exchange, 401, new WebsiteMessageResp("401", result.getMessage()));
                    return;
                }
                writeJson(exchange, 200, result.getTokenResponse());
            } catch (Exception e) {
                Mod.LOGGER.warn("Failed to write login response", e);
                try {
                    writeJson(exchange, 500, new WebsiteMessageResp("500", "Unknown error"));
                } catch (Exception ignored) {
                    exchange.close();
                }
            }
        });
    }

    private static void handleGetDataAsync(HttpExchange exchange) {
        GenerateResponse.generateJsonResponseAsync().whenComplete((dataJson, throwable) -> {
            try {
                if (throwable != null) {
                    Mod.LOGGER.warn("Failed to generate storage data response", throwable);
                    writeJson(exchange, 500, new WebsiteMessageResp("500", "Unknown error"));
                    return;
                }
                JsonElement data = JsonParser.parseString(dataJson);
                writeJson(exchange, 200, data);
            } catch (Exception e) {
                Mod.LOGGER.warn("Failed to write getData response", e);
                try {
                    writeJson(exchange, 500, new WebsiteMessageResp("500", "Unknown error"));
                } catch (Exception ignored) {
                    exchange.close();
                }
            }
        });
    }

    private static void handleWebsiteGetItemAsync(HttpExchange exchange, String authHeader, String body,
        boolean noPasswordEnabled) {
        AsyncTasks.supply(() -> {
            WebsiteAuth.AuthResult auth = WebsiteAuth.resolve(authHeader, noPasswordEnabled, SECRET_PATH);
            if (auth.errorMessage != null) {
                throw new WebsiteApiException(401, auth.errorMessage);
            }
            return WebsiteGetItemHandler.process(body, auth.username);
        }).whenComplete((resp, throwable) -> {
            try {
                if (throwable != null) {
                    Throwable cause = throwable instanceof RuntimeException && throwable.getCause() != null
                        ? throwable.getCause() : throwable;
                    if (cause instanceof WebsiteApiException apiException) {
                        writeJson(exchange, apiException.status,
                            new WebsiteMessageResp(String.valueOf(apiException.status), apiException.getMessage()));
                        return;
                    }
                    Mod.LOGGER.warn("Failed to handle website getItem request", throwable);
                    writeJson(exchange, 500, new WebsiteMessageResp("500", "Unknown error"));
                    return;
                }
                writeJson(exchange, 200, resp);
            } catch (Exception e) {
                Mod.LOGGER.warn("Failed to write website getItem response", e);
                try {
                    writeJson(exchange, 500, new WebsiteMessageResp("500", "Unknown error"));
                } catch (Exception ignored) {
                    exchange.close();
                }
            }
        });
    }

    private static void write(HttpExchange exchange, int status, String type, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private static void writeJson(HttpExchange exchange, int status, Object body) throws IOException {
        write(exchange, status, "application/json; charset=utf-8", GSON.toJson(body).getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isAutoStartWebsiteEnabled() {
        try {
            JsonObject root = JsonParser.parseString(AsyncFileIo.readString(CONFIG_JSON_PATH)).getAsJsonObject();
            JsonElement autoStartWebsite = root.get("autoStartWebsite");
            if (autoStartWebsite != null && autoStartWebsite.isJsonPrimitive()
                && autoStartWebsite.getAsJsonPrimitive().isBoolean()) {
                return autoStartWebsite.getAsBoolean();
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static int getPort() {
        try {
            JsonElement port =
                JsonParser.parseString(AsyncFileIo.readString(CONFIG_JSON_PATH)).getAsJsonObject().get("port");
            if (port != null && port.isJsonPrimitive() && port.getAsJsonPrimitive().isNumber()) {
                return port.getAsInt();
            }
        } catch (Exception ignored) {
        }
        return 7000;
    }

    private static int getExpireDay() {
        try {
            JsonElement expireDay =
                JsonParser.parseString(AsyncFileIo.readString(CONFIG_JSON_PATH)).getAsJsonObject().get("expireDay");
            if (expireDay != null && expireDay.isJsonPrimitive() && expireDay.getAsJsonPrimitive().isNumber()) {
                return expireDay.getAsInt();
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private static boolean isNoPasswordEnabled() {
        try {
            JsonElement noPassword =
                JsonParser.parseString(AsyncFileIo.readString(CONFIG_JSON_PATH)).getAsJsonObject().get("noPassword");
            if (noPassword != null && noPassword.isJsonPrimitive() && noPassword.getAsJsonPrimitive().isBoolean()) {
                return noPassword.getAsBoolean();
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
