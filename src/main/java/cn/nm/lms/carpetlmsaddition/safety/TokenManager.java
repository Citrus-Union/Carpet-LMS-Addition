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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import cn.nm.lms.carpetlmsaddition.lib.FileIo;

public class TokenManager {
    public static final String TOKEN_INVALID_MESSAGE = "Invalid token";
    public static final String TOKEN_EXPIRED_MESSAGE = "Token expired";
    private static final int KEY_LENGTH = 32;
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private static long dayToSeconds(int day) throws IllegalArgumentException {
        if (day <= 0) {
            return 5L;
        }
        return (long)day * 24 * 60 * 60;
    }

    public static String generateToken(Path path, String username, int expireDay) throws RuntimeException {
        try {
            long issuedAt = Instant.now().getEpochSecond();
            long expireSeconds = dayToSeconds(expireDay);
            long expiresAt = issuedAt + expireSeconds;

            byte[] secret = getOrCreateSecret(path);

            String header = base64UrlEncode(createHeader().toString().getBytes(StandardCharsets.UTF_8));
            String payload = base64UrlEncode(
                createPayload(username, issuedAt, expiresAt).toString().getBytes(StandardCharsets.UTF_8));
            String signingInput = header + "." + payload;
            String signature = base64UrlEncode(sign(signingInput, secret));
            return signingInput + "." + signature;
        } catch (IOException e) {
            throw new RuntimeException("io");
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("sign error");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("invalid argument");
        }
    }

    public static String verifyToken(Path path, String token) {
        try {
            String[] parts = splitToken(token);
            String signingInput = parts[0] + "." + parts[1];

            byte[] secret = getOrCreateSecret(path);

            byte[] expectedSignature = sign(signingInput, secret);
            byte[] actualSignature = BASE64_URL_DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
                throw new RuntimeException(TOKEN_INVALID_MESSAGE);
            }

            JsonObject header = parseJsonObject(decodePart(parts[0]));
            if (!"HS256".equals(getStringClaim(header, "alg"))) {
                throw new RuntimeException(TOKEN_INVALID_MESSAGE);
            }

            JsonObject claims = parseJsonObject(decodePart(parts[1]));
            long expiresAt = getLongClaim(claims, "exp");
            if (expiresAt < Instant.now().getEpochSecond()) {
                throw new RuntimeException(TOKEN_EXPIRED_MESSAGE);
            }

            String tokenUsername = getStringClaim(claims, "sub");
            if (tokenUsername == null || tokenUsername.isBlank()) {
                throw new RuntimeException(TOKEN_INVALID_MESSAGE);
            }

            return tokenUsername;
        } catch (IOException e) {
            throw new RuntimeException("io");
        } catch (GeneralSecurityException | IllegalArgumentException | JsonParseException | IllegalStateException
            | UnsupportedOperationException e) {
            throw new RuntimeException("parse error");
        }
    }

    private static String[] splitToken(String token) {
        if (token == null) {
            throw new IllegalArgumentException();
        }
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3 || parts[0].isEmpty() || parts[1].isEmpty() || parts[2].isEmpty()) {
            throw new IllegalArgumentException();
        }
        return parts;
    }

    private static JsonObject createHeader() {
        JsonObject header = new JsonObject();
        header.addProperty("alg", "HS256");
        header.addProperty("typ", "JWT");
        return header;
    }

    private static JsonObject createPayload(String username, long issuedAt, long expiresAt) {
        JsonObject payload = new JsonObject();
        payload.addProperty("sub", username);
        payload.addProperty("iat", issuedAt);
        payload.addProperty("exp", expiresAt);
        return payload;
    }

    private static String decodePart(String part) {
        return new String(BASE64_URL_DECODER.decode(part), StandardCharsets.UTF_8);
    }

    private static String base64UrlEncode(byte[] bytes) {
        return BASE64_URL_ENCODER.encodeToString(bytes);
    }

    private static byte[] sign(String signingInput, byte[] secret) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
        return mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII));
    }

    private static JsonObject parseJsonObject(String json) {
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException();
        }
        return root.getAsJsonObject();
    }

    private static String getStringClaim(JsonObject object, String name) {
        JsonElement element = object.get(name);
        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }
        return element.getAsString();
    }

    private static long getLongClaim(JsonObject object, String name) {
        JsonElement element = object.get(name);
        if (element == null || !element.isJsonPrimitive()) {
            throw new IllegalArgumentException();
        }
        return element.getAsLong();
    }

    private static byte[] getOrCreateSecret(Path path) throws IOException {
        if (Files.exists(path)) {
            String secret = FileIo.readString(path).trim();
            if (!secret.isBlank()) {
                try {
                    byte[] secretBytes = Base64.getDecoder().decode(secret);
                    if (secretBytes.length == KEY_LENGTH) {
                        return secretBytes;
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        byte[] newSecret = generateSecret();

        String base64 = Base64.getEncoder().encodeToString(newSecret);

        FileIo.writeString(path, base64);
        return newSecret;
    }

    private static byte[] generateSecret() {
        byte[] bytes = new byte[KEY_LENGTH];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
