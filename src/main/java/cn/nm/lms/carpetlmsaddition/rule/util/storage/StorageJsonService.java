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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.Mod;
import cn.nm.lms.carpetlmsaddition.lib.AsyncFileIo;
import cn.nm.lms.carpetlmsaddition.lib.getvalue.GetPaths;

final class StorageJsonService {
    private StorageJsonService() {}

    static Storage.PreparedInputs prepareInputs() {
        if (!ensureDefaultConfigExists()) {
            return Storage.PreparedInputs.EMPTY;
        }

        JsonObject config = readConfigJsonObject();
        if (config == null) {
            return Storage.PreparedInputs.EMPTY;
        }

        try {
            AsyncFileIo.createDirectories(Storage.storageDataPath);
            AsyncFileIo.createDirectories(Storage.storageDir);
        } catch (IOException e) {
            Mod.LOGGER.warn("Failed to create storage dirs: {}, {}", Storage.storageDataPath, Storage.storageDir);
            return Storage.PreparedInputs.EMPTY;
        }

        if (!config.has("storageList") || !config.get("storageList").isJsonArray()) {
            Mod.LOGGER.warn("Missing or invalid storageList in {}", Storage.configJsonPath);
            return Storage.PreparedInputs.EMPTY;
        }

        JsonArray storageList = config.getAsJsonArray("storageList");
        List<Storage.PreparedStorage> inputs = new ArrayList<>();

        for (JsonElement element : storageList) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                Mod.LOGGER.warn("Skip invalid storage entry: {}", element);
                continue;
            }

            String storageFileName = element.getAsString();
            Path storageFile = Storage.storageDir.resolve(storageFileName);
            if (!ensureStorageFileExists(storageFile)) {
                Mod.LOGGER.warn("Failed to prepare storage file: {}", storageFileName);
                continue;
            }

            try {
                JsonElement root = JsonParser.parseString(AsyncFileIo.readString(storageFile));
                if (!root.isJsonObject()) {
                    Mod.LOGGER.warn("Invalid storage JSON format: {}", storageFileName);
                    continue;
                }
                inputs.add(new Storage.PreparedStorage(storageFileName, root.getAsJsonObject()));
            } catch (Exception e) {
                Mod.LOGGER.warn("Failed to process storage file: {}", storageFileName);
            }
        }

        return new Storage.PreparedInputs(storageList.size(), inputs);
    }

    static void saveToFile(Path savePath, Map<Item, Storage.ItemCount> items, List<Storage.Position> errors)
        throws IOException {
        AsyncFileIo.writeString(savePath, toOutputJson(items, errors));
    }

    private static String toOutputJson(Map<Item, Storage.ItemCount> items, List<Storage.Position> errors) {
        JsonObject root = new JsonObject();

        JsonObject itemsObject = new JsonObject();
        items.forEach((item, itemCount) -> {
            Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
            JsonObject oneItem = new JsonObject();
            oneItem.addProperty("count", itemCount.count);

            JsonObject positionsByDimension = new JsonObject();
            Map<String, JsonArray> groupedPositions = new HashMap<>();
            itemCount.positionsCount.forEach((position, positionCount) -> {
                String dimension = Storage.dimensionToSting.get(position.dimension);
                if (dimension == null) {
                    return;
                }

                JsonArray oneDimensionArray = groupedPositions.computeIfAbsent(dimension, key -> new JsonArray());
                JsonObject onePosition = new JsonObject();
                JsonObject pos = new JsonObject();
                pos.addProperty("x", position.pos.getX());
                pos.addProperty("y", position.pos.getY());
                pos.addProperty("z", position.pos.getZ());
                onePosition.add("pos", pos);
                onePosition.addProperty("count", positionCount);
                oneDimensionArray.add(onePosition);
            });

            groupedPositions.forEach(positionsByDimension::add);
            oneItem.add("positionsByDimension", positionsByDimension);
            itemsObject.add(itemId.toString(), oneItem);
        });
        root.add("items", itemsObject);

        JsonArray errorsArray = new JsonArray();
        errors.forEach(position -> errorsArray.add(position.toJson()));
        root.add("errors", errorsArray);

        return Storage.PRETTY_GSON.toJson(root);
    }

    private static boolean ensureDefaultConfigExists() {
        if (AsyncFileIo.exists(Storage.configJsonPath)) {
            return true;
        }

        JsonObject defaultConfig = new JsonObject();
        defaultConfig.addProperty("port", 7000);
        defaultConfig.addProperty("autoStartWebsite", false);
        defaultConfig.addProperty("noPassword", false);
        defaultConfig.addProperty("expireDay", 0);
        JsonArray storageList = new JsonArray();
        storageList.add("example.json");
        defaultConfig.add("storageList", storageList);

        try {
            AsyncFileIo.createDirectories(GetPaths.getLmsWorldPath());
            writePrettyJson(Storage.configJsonPath, defaultConfig);
            Mod.LOGGER.info("Generated default storage config: {}", Storage.configJsonPath);
            return true;
        } catch (IOException e) {
            Mod.LOGGER.warn("Failed to generate default storage config: {}", Storage.configJsonPath);
            return false;
        }
    }

    private static boolean ensureStorageFileExists(Path storageFile) {
        if (AsyncFileIo.exists(storageFile)) {
            return true;
        }

        try {
            AsyncFileIo.createParentDirectories(storageFile);
            JsonObject defaultStorage = new JsonObject();
            if ("example.json".equals(storageFile.getFileName().toString())) {
                JsonArray overworld = new JsonArray();
                JsonArray examplePos = new JsonArray();
                examplePos.add(0);
                examplePos.add(0);
                examplePos.add(0);
                overworld.add(examplePos);
                defaultStorage.add("overworld", overworld);
                defaultStorage.add("end", new JsonArray());
                defaultStorage.add("nether", new JsonArray());
            }
            writePrettyJson(storageFile, defaultStorage);
            Mod.LOGGER.info("Generated empty storage list file: {}", storageFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void writePrettyJson(Path path, JsonElement jsonElement) throws IOException {
        AsyncFileIo.writeString(path, Storage.PRETTY_GSON.toJson(jsonElement));
    }

    private static @Nullable JsonObject readConfigJsonObject() {
        if (!AsyncFileIo.exists(Storage.configJsonPath)) {
            Mod.LOGGER.warn("Config file not found: {}", Storage.configJsonPath);
            return null;
        }
        try {
            JsonElement root = JsonParser.parseString(AsyncFileIo.readString(Storage.configJsonPath));
            if (!root.isJsonObject()) {
                Mod.LOGGER.warn("Invalid config JSON (root is not object): {}", Storage.configJsonPath);
                return null;
            }
            return root.getAsJsonObject();
        } catch (Exception e) {
            Mod.LOGGER.warn("Invalid config JSON format: {}", Storage.configJsonPath);
            return null;
        }
    }
}
