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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public final class JsonFileIo {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private JsonFileIo() {}

    public static JsonObject readObjectOrEmpty(Path path) throws IOException {
        if (!AsyncFileIo.exists(path)) {
            return new JsonObject();
        }
        return readObject(path);
    }

    public static JsonObject putString(Path path, String value, String... fieldPath) throws IOException {
        return put(path, json -> json.addProperty(fieldPath[fieldPath.length - 1], value), fieldPath);
    }

    public static JsonObject putElement(Path path, JsonElement value, String... fieldPath) throws IOException {
        return put(path, json -> json.add(fieldPath[fieldPath.length - 1], value), fieldPath);
    }

    private static JsonObject put(Path path, JsonObjectWriter writer, String... fieldPath) throws IOException {
        if (fieldPath.length == 0) {
            throw new IOException("JSON field path cannot be empty: " + path);
        }
        JsonObject root = readObjectOrEmpty(path);
        JsonObject target = root;
        for (int i = 0; i < fieldPath.length - 1; i++) {
            String parentKey = fieldPath[i];
            JsonElement parent = target.get(parentKey);
            if (parent == null || parent.isJsonNull()) {
                JsonObject created = new JsonObject();
                target.add(parentKey, created);
                target = created;
            } else if (parent.isJsonObject()) {
                target = parent.getAsJsonObject();
            } else {
                throw new IOException("JSON field must be an object: " + parentKey + " in " + path);
            }
        }
        writer.write(target);
        write(path, root);
        return root;
    }

    public static JsonObject readObject(Path path) throws IOException {
        JsonElement root = read(path);
        if (!root.isJsonObject()) {
            throw new IOException("JSON root must be an object: " + path);
        }
        return root.getAsJsonObject();
    }

    public static JsonElement read(Path path) throws IOException {
        try {
            return JsonParser.parseString(AsyncFileIo.readString(path));
        } catch (JsonParseException | IllegalStateException e) {
            throw new IOException("Invalid JSON: " + path, e);
        }
    }

    public static void write(Path path, JsonElement json) throws IOException {
        AsyncFileIo.writeString(path, GSON.toJson(json));
    }

    private interface JsonObjectWriter {
        void write(JsonObject json);
    }
}
