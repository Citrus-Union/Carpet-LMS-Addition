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
package cn.nm.lms.carpetlmsaddition.storage.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.Mod;
import cn.nm.lms.carpetlmsaddition.lib.FileIo;
import cn.nm.lms.carpetlmsaddition.lib.ItemRegistryCompat;

final class StorageJsonService {
    private StorageJsonService() {}

    static void prepareDefaultFilesOnWorldLoad() {
        ensureDefaultConfigExists();

        JsonObject config = readConfigJsonObject();
        if (config == null) {
            return;
        }
        JsonArray storageList = getStorageList(config);
        if (storageList == null) {
            return;
        }
        for (JsonElement element : storageList) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
                && "example.json".equals(element.getAsString())) {
                ensureDefaultStorageExampleExists();
                return;
            }
        }
    }

    static Storage.PreparedInputs prepareInputs() {
        JsonObject config = readConfigJsonObject();
        if (config == null) {
            return Storage.PreparedInputs.EMPTY;
        }

        try {
            Files.createDirectories(Storage.storageDir);
        } catch (IOException e) {
            Mod.LOGGER.warn("Failed to create storage dir: {}", Storage.storageDir);
            return Storage.PreparedInputs.EMPTY;
        }

        JsonArray storageList = getStorageList(config);
        if (storageList == null) {
            return Storage.PreparedInputs.EMPTY;
        }

        List<Storage.PreparedStorage> inputs = new ArrayList<>();

        for (JsonElement element : storageList) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                Mod.LOGGER.warn("Skip invalid storage entry: {}", element);
                continue;
            }

            String storageFileName = element.getAsString();
            Path storageFile = Storage.storageDir.resolve(storageFileName);
            if (!Files.exists(storageFile)) {
                Mod.LOGGER.warn("Storage file not found: {}", storageFile);
                continue;
            }

            try {
                JsonElement root = JsonParser.parseString(FileIo.readString(storageFile));
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

    private static @Nullable JsonArray getStorageList(JsonObject config) {
        if (!config.has("storageList") || !config.get("storageList").isJsonArray()) {
            Mod.LOGGER.warn("Missing or invalid storageList in {}", Storage.configJsonPath);
            return null;
        }
        return config.getAsJsonArray("storageList");
    }

    static JsonArray toOutputItemsArray(Map<Item, Storage.ItemCount> items) {
        JsonArray itemsArray = new JsonArray();
        items.entrySet().stream().map(entry -> {
            Identifier itemId = BuiltInRegistries.ITEM.getKey(entry.getKey());
            String compactItemId = ItemRegistryCompat.compactItemId(itemId.toString());
            JsonObject oneItem = new JsonObject();
            oneItem.addProperty("i", compactItemId);
            oneItem.addProperty("c", entry.getValue().count);
            return oneItem;
        }).sorted(Comparator.comparing(oneItem -> oneItem.get("i").getAsString())).forEach(itemsArray::add);

        return itemsArray;
    }

    static JsonObject toOutputErrorsObject(List<Storage.Position> errors) {
        Map<ResourceKey<Level>, JsonArray> errorsByDimension = new HashMap<>();
        for (Storage.StorageDimension dimension : Storage.DIMENSIONS) {
            errorsByDimension.put(dimension.key(), new JsonArray());
        }
        errors.forEach(position -> {
            JsonArray oneError = new JsonArray();
            oneError.add(position.pos.getX());
            oneError.add(position.pos.getY());
            oneError.add(position.pos.getZ());
            JsonArray dimensionErrors = errorsByDimension.get(position.dimension);
            if (dimensionErrors != null) {
                dimensionErrors.add(oneError);
            }
        });
        JsonObject errorsObject = new JsonObject();
        for (Storage.StorageDimension dimension : Storage.DIMENSIONS) {
            JsonArray dimensionErrors = errorsByDimension.get(dimension.key());
            if (!dimensionErrors.isEmpty()) {
                errorsObject.add(dimension.outputKey(), dimensionErrors);
            }
        }

        return errorsObject;
    }

    private static boolean ensureDefaultConfigExists() {
        if (Files.exists(Storage.configJsonPath)) {
            return true;
        }

        JsonObject defaultConfig = new JsonObject();
        defaultConfig.addProperty("port", 7000);
        defaultConfig.addProperty("autoStartWebsite", false);
        defaultConfig.addProperty("customWebsite", false);
        defaultConfig.addProperty("noPassword", false);
        defaultConfig.addProperty("expireDay", 0);
        JsonArray storageList = new JsonArray();
        storageList.add("example.json");
        defaultConfig.add("storageList", storageList);

        try {
            FileIo.writeString(Storage.configJsonPath, Storage.PRETTY_GSON.toJson(defaultConfig));
            Mod.LOGGER.info("Generated default storage config: {}", Storage.configJsonPath);
            return true;
        } catch (IOException e) {
            Mod.LOGGER.warn("Failed to generate default storage config: {}", Storage.configJsonPath);
            return false;
        }
    }

    private static boolean ensureDefaultStorageExampleExists() {
        Path storageFile = Storage.storageDir.resolve("example.json");
        if (Files.exists(storageFile)) {
            return true;
        }

        try {
            JsonObject defaultStorage = new JsonObject();
            JsonArray overworld = new JsonArray();
            JsonArray examplePos = new JsonArray();
            examplePos.add(0);
            examplePos.add(0);
            examplePos.add(0);
            overworld.add(examplePos);
            defaultStorage.add("overworld", overworld);
            defaultStorage.add("end", new JsonArray());
            defaultStorage.add("nether", new JsonArray());
            FileIo.writeString(storageFile, Storage.PRETTY_GSON.toJson(defaultStorage));
            Mod.LOGGER.info("Generated default storage list file: {}", storageFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static @Nullable JsonObject readConfigJsonObject() {
        if (!Files.exists(Storage.configJsonPath)) {
            Mod.LOGGER.warn("Config file not found: {}", Storage.configJsonPath);
            return null;
        }
        try {
            JsonElement root = JsonParser.parseString(FileIo.readString(Storage.configJsonPath));
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
