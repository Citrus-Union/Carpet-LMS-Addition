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
package cn.nm.lms.carpetlmsaddition.lib;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.lib.getvalue.GetPaths;

public final class PlayerConfig {
    private static JsonObject root;
    private static Path loadedFile;

    @Nullable
    public static synchronized String get(UUID playerUUID, String configName) {
        JsonObject data = ensureLoaded();
        JsonObject perConfig = data.getAsJsonObject(configName);
        if (perConfig == null) {
            return null;
        }
        JsonElement raw = perConfig.get(playerUUID.toString());
        if (raw == null || raw.isJsonNull() || !raw.isJsonPrimitive()) {
            return null;
        }
        return raw.getAsString();
    }

    public static synchronized void set(UUID playerUUID, String configName, String value) {
        Path file = currentFile();
        try {
            root = JsonFileIo.putString(file, value, configName, playerUUID.toString());
            loadedFile = file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static synchronized Boolean getBoolean(UUID playerUUID, String configName) {
        JsonObject data = ensureLoaded();
        JsonObject perConfig = data.getAsJsonObject(configName);
        if (perConfig == null) {
            return null;
        }
        JsonElement raw = perConfig.get(playerUUID.toString());
        if (raw == null || raw.isJsonNull() || !raw.isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive primitive = raw.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        return null;
    }

    public static synchronized void setBoolean(UUID playerUUID, String configName, boolean value) {
        Path file = currentFile();
        try {
            root = JsonFileIo.putElement(file, new JsonPrimitive(value), configName, playerUUID.toString());
            loadedFile = file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static synchronized <E extends Enum<E>> E getEnum(UUID playerUUID, String configName, Class<E> enumClass) {
        String configured = get(playerUUID, configName);
        if (configured == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, configured.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static synchronized void setEnum(UUID playerUUID, String configName, Enum<?> value) {
        set(playerUUID, configName, value.name().toLowerCase(Locale.ROOT));
    }

    @Nullable
    public static synchronized Set<String> getStringSet(UUID playerUUID, String configName) {
        JsonObject data = ensureLoaded();
        JsonObject perConfig = data.getAsJsonObject(configName);
        if (perConfig == null) {
            return null;
        }
        JsonElement raw = perConfig.get(playerUUID.toString());
        if (raw == null || raw.isJsonNull()) {
            return null;
        }
        if (!raw.isJsonArray()) {
            return null;
        }
        Set<String> values = new LinkedHashSet<>();
        for (JsonElement value : raw.getAsJsonArray()) {
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                return null;
            }
            values.add(value.getAsString());
        }
        return values;
    }

    public static synchronized void setStringSet(UUID playerUUID, String configName, Set<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        Path file = currentFile();
        try {
            root = JsonFileIo.putElement(file, array, configName, playerUUID.toString());
            loadedFile = file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized Set<String> addToStringSet(UUID playerUUID, String configName, String value) {
        Set<String> values = getStringSet(playerUUID, configName);
        if (values == null) {
            values = new LinkedHashSet<>();
        }
        values.add(value);
        setStringSet(playerUUID, configName, values);
        return values;
    }

    public static synchronized Set<String> removeFromStringSet(UUID playerUUID, String configName, String value) {
        Set<String> values = getStringSet(playerUUID, configName);
        if (values == null) {
            values = new LinkedHashSet<>();
        }
        values.remove(value);
        setStringSet(playerUUID, configName, values);
        return values;
    }

    private static synchronized JsonObject ensureLoaded() {
        Path file = currentFile();
        if (root != null && file.equals(loadedFile)) {
            return root;
        }
        try {
            root = JsonFileIo.readObjectOrEmpty(file);
            loadedFile = file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return root;
    }

    private static Path currentFile() {
        return GetPaths.getLmsWorldDataPath().resolve("playerConfig.json");
    }

    public static boolean isPlayerEnabled(RuleSetting setting, UUID playerUUID, String configName) {
        return switch (setting) {
            case TRUE -> true;
            case FALSE -> false;
            case CUSTOM -> isPlayerSettingTrue(playerUUID, configName);
        };
    }

    private static boolean isPlayerSettingTrue(UUID playerUUID, String configName) {
        Boolean configured = PlayerConfig.getBoolean(playerUUID, configName);
        return Boolean.TRUE.equals(configured);
    }

    public enum RuleSetting {
        TRUE, FALSE, CUSTOM
    }
}
