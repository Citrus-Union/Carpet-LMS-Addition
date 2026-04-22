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

import java.nio.file.Path;

import cn.nm.lms.carpetlmsaddition.safety.TokenManager;

final class WebsiteAuth {
    private WebsiteAuth() {}

    static AuthResult resolve(String authorizationHeader, boolean noPasswordEnabled, Path secretPath) {
        String token = extractBearerToken(authorizationHeader);
        if (token == null) {
            if (noPasswordEnabled) {
                return new AuthResult(null, null);
            }
            return new AuthResult(null, "Missing or invalid Authorization header");
        }

        try {
            return new AuthResult(TokenManager.verifyToken(secretPath, token), null);
        } catch (RuntimeException e) {
            if (noPasswordEnabled) {
                return new AuthResult(null, null);
            }
            String message = TokenManager.TOKEN_INVALID_MESSAGE;
            if (TokenManager.TOKEN_EXPIRED_MESSAGE.equals(e.getMessage())) {
                message = TokenManager.TOKEN_EXPIRED_MESSAGE;
            }
            return new AuthResult(null, message);
        }
    }

    private static String extractBearerToken(String authorizationHeader) {
        String bearerPrefix = "Bearer ";
        if (authorizationHeader == null || !authorizationHeader.startsWith(bearerPrefix)) {
            return null;
        }
        String token = authorizationHeader.substring(bearerPrefix.length()).trim();
        return token.isEmpty() ? null : token;
    }

    static final class AuthResult {
        final String username;
        final String errorMessage;

        AuthResult(String username, String errorMessage) {
            this.username = username;
            this.errorMessage = errorMessage;
        }
    }
}
