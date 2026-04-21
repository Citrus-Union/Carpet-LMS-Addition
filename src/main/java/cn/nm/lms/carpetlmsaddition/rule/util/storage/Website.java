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
package cn.nm.lms.carpetlmsaddition.rule.util.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
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
import cn.nm.lms.carpetlmsaddition.safety.Password;
import cn.nm.lms.carpetlmsaddition.safety.TokenManager;

public class Website {
    private static final Gson GSON = new Gson();
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Path SECRET_PATH = GetPaths.getLmsConfigSecretPath().resolve("storage");
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

    private static void resetAutoUpdateDataState() {
        autoUpdateDataTicks = 0;
        lastAutoUpdateInterval = 0;
    }

    public static void autoStartFromConfigAfterWorldLoaded() {
        if (isAutoStartWebsiteEnabled()) {
            startServer();
        }
    }

    private static boolean isAutoStartWebsiteEnabled() {
        try {
            JsonObject root = JsonParser.parseString(AsyncFileIo.readString(Storage.configJsonPath)).getAsJsonObject();
            JsonElement autoStartWebsite = root.get("autoStartWebsite");
            if (autoStartWebsite != null && autoStartWebsite.isJsonPrimitive()
                && autoStartWebsite.getAsJsonPrimitive().isBoolean()) {
                return autoStartWebsite.getAsBoolean();
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static int getPort() {
        try {
            JsonElement port =
                JsonParser.parseString(AsyncFileIo.readString(Storage.configJsonPath)).getAsJsonObject().get("port");
            if (port != null && port.isJsonPrimitive() && port.getAsJsonPrimitive().isNumber()) {
                return port.getAsInt();
            }
        } catch (Exception e) {
        }
        return 7000;
    }

    private static void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("/api/storage/getData".equals(path)) {
            if (!"GET".equals(method)) {
                writeJson(exchange, 405, new MessageResp("405", "Method Not Allowed"));
                return;
            }
            if (!isNoPasswordEnabled()) {
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                String token = extractBearerToken(authHeader);
                if (token == null) {
                    writeJson(exchange, 401, new MessageResp("401", "Missing or invalid Authorization header"));
                    return;
                }
                try {
                    TokenManager.verifyToken(SECRET_PATH, token);
                } catch (RuntimeException e) {
                    String message = TokenManager.TOKEN_INVALID_MESSAGE;
                    if (TokenManager.TOKEN_EXPIRED_MESSAGE.equals(e.getMessage())) {
                        message = TokenManager.TOKEN_EXPIRED_MESSAGE;
                    }
                    writeJson(exchange, 401, new MessageResp("401", message));
                    return;
                }
            }
            handleGetDataAsync(exchange);
            return;
        }

        if ("/api/login".equals(path)) {
            if (!"POST".equals(method)) {
                writeJson(exchange, 405, new MessageResp("405", "Method Not Allowed"));
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

        String file = "/".equals(path) ? "/index.html" : path;
        if (file.contains("..")) {
            write(exchange, 400, "text/plain; charset=utf-8", "Bad Request".getBytes(StandardCharsets.UTF_8));
            return;
        }
        String resource = "websites/storage" + file;
        byte[] content;
        try (InputStream in = Website.class.getClassLoader().getResourceAsStream(resource)) {
            content = in == null ? null : in.readAllBytes();
        }
        if (content == null && !file.contains(".")) {
            try (InputStream in = Website.class.getClassLoader().getResourceAsStream("websites/storage/index.html")) {
                content = in == null ? null : in.readAllBytes();
            }
            resource = "websites/storage/index.html";
        }
        if (content == null) {
            write(exchange, 404, "text/plain; charset=utf-8", "Not Found".getBytes(StandardCharsets.UTF_8));
            return;
        }
        String contentType = "application/octet-stream";
        if (resource.endsWith(".html")) {
            contentType = "text/html; charset=utf-8";
        } else if (resource.endsWith(".css")) {
            contentType = "text/css; charset=utf-8";
        } else if (resource.endsWith(".js")) {
            contentType = "application/javascript; charset=utf-8";
        } else if (resource.endsWith(".json")) {
            contentType = "application/json; charset=utf-8";
        }
        write(exchange, 200, contentType, content);
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

    private static void handleLoginAsync(HttpExchange exchange, String body) {
        CompletableFuture<Password.Result> authFuture =
            Password.LoginRequest.authenticateAsync(body, SECRET_PATH, getExpireDay());
        authFuture.whenComplete((result, throwable) -> {
            try {
                if (throwable != null) {
                    Mod.LOGGER.warn("Unknown error while authenticating", throwable);
                    writeJson(exchange, 500, new MessageResp("500", "Unknown error"));
                    return;
                }
                if (!result.isSuccess()) {
                    writeJson(exchange, 401, new MessageResp("401", result.getMessage()));
                    return;
                }
                writeJson(exchange, 200, result.getTokenResponse());
            } catch (Exception e) {
                Mod.LOGGER.warn("Failed to write login response", e);
                try {
                    writeJson(exchange, 500, new MessageResp("500", "Unknown error"));
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
                    writeJson(exchange, 500, new MessageResp("500", "Unknown error"));
                    return;
                }
                JsonElement data = JsonParser.parseString(dataJson);
                writeJson(exchange, 200, data);
            } catch (Exception e) {
                Mod.LOGGER.warn("Failed to write getData response", e);
                try {
                    writeJson(exchange, 500, new MessageResp("500", "Unknown error"));
                } catch (Exception ignored) {
                    exchange.close();
                }
            }
        });
    }

    private static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private static int getExpireDay() {
        try {
            JsonElement port = JsonParser.parseString(AsyncFileIo.readString(Storage.configJsonPath)).getAsJsonObject()
                .get("expireDay");
            if (port != null && port.isJsonPrimitive() && port.getAsJsonPrimitive().isNumber()) {
                return port.getAsInt();
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private static boolean isNoPasswordEnabled() {
        try {
            JsonElement noPassword = JsonParser.parseString(AsyncFileIo.readString(Storage.configJsonPath))
                .getAsJsonObject().get("noPassword");
            if (noPassword != null && noPassword.isJsonPrimitive() && noPassword.getAsJsonPrimitive().isBoolean()) {
                return noPassword.getAsBoolean();
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static class MessageResp {
        String status;
        String message;

        MessageResp(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}
