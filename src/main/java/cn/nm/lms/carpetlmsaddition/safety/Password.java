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
package cn.nm.lms.carpetlmsaddition.safety;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jspecify.annotations.Nullable;
import org.mindrot.jbcrypt.BCrypt;

import cn.nm.lms.carpetlmsaddition.Mod;
import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;

public class Password {
    private static final Gson GSON = new Gson();

    private static boolean verified(String inputPassword, String storedHash) {
        return BCrypt.checkpw(inputPassword, storedHash);
    }

    private static String generateHash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private static Result setPasswordSync(String password, String username) {
        Result passwordCheck = isBadPassword(password);
        if (!passwordCheck.isSuccess()) {
            return passwordCheck;
        }
        if (username == null || username.isBlank()) {
            return Result.failure("Username is empty");
        }
        String passwordHash = generateHash(password);

        try {
            UserPasswordStore.setPasswordHash(username, passwordHash);
            return Result.success();
        } catch (UserPasswordStore.UserDataException e) {
            Mod.LOGGER.warn("User data error while setting password", e);
            return Result.failure("User data error");
        } catch (Exception e) {
            Mod.LOGGER.warn("Unknown error while setting password", e);
            return Result.failure("Unknown error");
        }
    }

    public static CompletableFuture<Result> setPasswordAsync(String password, String username) {
        return AsyncTasks.supply(() -> setPasswordSync(password, username));
    }

    private static Result authenticateSync(String username, String inputPassword, Path secret, int expireDay) {
        if (username == null || username.isBlank()) {
            return Result.failure("Username is empty");
        }
        if (inputPassword == null || inputPassword.isBlank()) {
            return Result.failure("Password is empty");
        }
        try {
            String passwordHash = UserPasswordStore.getPasswordHash(username);
            if (passwordHash == null) {
                return Result.failure("Invalid username or password");
            }
            if (verified(inputPassword, passwordHash)) {
                String token = TokenManager.generateToken(secret, username, expireDay);
                return Result.success(username, token);
            }
            return Result.failure("Invalid username or password");
        } catch (IllegalArgumentException e) {
            Mod.LOGGER.warn("Invalid user password hash while authenticating", e);
            return Result.failure("User data error");
        } catch (UserPasswordStore.UserDataException e) {
            Mod.LOGGER.warn("User data error while authenticating", e);
            return Result.failure("User data error");
        } catch (Exception e) {
            Mod.LOGGER.warn("Unknown error while authenticating", e);
            return Result.failure("Unknown error");
        }
    }

    private static CompletableFuture<Result> authenticateAsync(String username, String inputPassword, Path secret,
        int expireDay) {
        return AsyncTasks.supply(() -> authenticateSync(username, inputPassword, secret, expireDay));
    }

    private static Result isBadPassword(String password) {
        if (password == null || password.isBlank()) {
            return Result.failure("Password cannot be empty");
        }
        return Result.success();
    }

    public static final class Result {
        private final boolean success;
        private final String message;
        private final String username;
        private final String token;

        private Result(boolean success, String message, String username, String token) {
            this.success = success;
            this.message = message;
            this.username = username;
            this.token = token;
        }

        public static Result success(String username, String token) {
            return new Result(true, null, username, token);
        }

        public static Result success() {
            return new Result(true, null, null, null);
        }

        public static Result failure(String message) {
            return new Result(false, message, null, null);
        }

        public boolean isSuccess() {
            return this.success;
        }

        public String getMessage() {
            return this.message;
        }

        public JsonElement getTokenResponse() {
            JsonObject obj = new JsonObject();
            obj.addProperty("username", this.username);
            obj.addProperty("token", this.token);
            return obj;
        }
    }

    public static class LoginRequest {
        /**
         * null means json error
         */
        @Nullable
        private String username;

        /**
         * null means json error
         */
        @Nullable
        private String password;

        public LoginRequest(String json) {
            try {
                LoginRequest temp = GSON.fromJson(json, LoginRequest.class);
                if (temp == null) {
                    this.username = null;
                    this.password = null;
                    return;
                }
                this.username = temp.username;
                this.password = temp.password;
            } catch (Exception e) {
                this.username = null;
                this.password = null;
            }
        }

        public static CompletableFuture<Result> authenticateAsync(String json, Path secret, int expireDay) {
            LoginRequest loginRequest = new LoginRequest(json);
            return loginRequest.authenticateAsync(secret, expireDay);
        }

        public CompletableFuture<Result> authenticateAsync(Path secret, int expireDay) {
            return Password.authenticateAsync(this.username, this.password, secret, expireDay);
        }
    }
}
