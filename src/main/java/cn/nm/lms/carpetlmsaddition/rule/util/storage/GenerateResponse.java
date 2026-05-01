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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import cn.nm.lms.carpetlmsaddition.Mod;
import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;
import cn.nm.lms.carpetlmsaddition.lib.getvalue.GetPaths;

public class GenerateResponse {
    private static final Path storageDataPath = GetPaths.getLmsWorldDataPath().resolve("checkStorageData.json");

    public static CompletableFuture<String> generateJsonResponseAsync() {
        return AsyncTasks.supply(() -> {
            String result = readMergedResponse();
            if (result != null) {
                return result;
            }
            return "[]";
        });
    }

    private static String readMergedResponse() {
        try {
            if (!Files.exists(storageDataPath)) {
                return null;
            }
            JsonElement root = JsonParser.parseString(Files.readString(storageDataPath));
            if (!root.isJsonArray()) {
                Mod.LOGGER.warn("Invalid merged storage data file format: {}", storageDataPath);
                return null;
            }
            return root.toString();
        } catch (Exception e) {
            Mod.LOGGER.warn("Failed to read merged storage data file: {}", storageDataPath);
            return null;
        }
    }
}
