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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.Mod;
import cn.nm.lms.carpetlmsaddition.lib.Utils;

public final class StorageContainerReader {
    public static List<ConfiguredStorageSnapshots> collectConfiguredStorageSnapshots() {
        return collectConfiguredStorageSnapshotsFromInputs();
    }

    private static List<ConfiguredStorageSnapshots> collectConfiguredStorageSnapshotsFromInputs() {
        Storage.PreparedInputs prepared = StorageJsonService.prepareInputs();
        List<ConfiguredStorageSnapshots> storages = new ArrayList<>();
        for (Storage.PreparedStorage storageInput : prepared.inputs) {
            List<Storage.Position> errors = new ArrayList<>();
            try {
                List<SlotSnapshot> snapshots = collectSnapshots(storageInput.pos, errors);
                storages.add(new ConfiguredStorageSnapshots(storageInput.fileName, snapshots, errors));
            } catch (Exception e) {
                Mod.LOGGER.warn("Failed to collect snapshots from storage file: {}", storageInput.fileName);
            }
        }
        return storages;
    }

    private static List<SlotSnapshot> collectSnapshots(JsonObject object, List<Storage.Position> errors) {
        List<Storage.Position> positions = collectPositions(object);
        return Utils.runOnServerThread(() -> snapshotPositions(positions, errors));
    }

    private static List<Storage.Position> collectPositions(JsonObject object) {
        List<Storage.Position> positions = new ArrayList<>();
        for (Storage.StorageDimension dimension : Storage.DIMENSIONS) {
            if (!object.has(dimension.configName()) || !object.get(dimension.configName()).isJsonArray()) {
                Mod.LOGGER.warn("Missing or invalid dimension array: {}", dimension.configName());
                continue;
            }

            addDimensionPositions(object.getAsJsonArray(dimension.configName()), dimension, positions);
        }

        return positions;
    }

    private static void addDimensionPositions(JsonArray array, Storage.StorageDimension dimension,
        List<Storage.Position> positions) {
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);

            BlockPos pos = parseBlockPos(element);
            if (pos == null) {
                Mod.LOGGER.warn("Invalid array at {}: {}", dimension.configName(), Objects.toString(element, ""));
                continue;
            }

            positions.add(new Storage.Position(dimension.key(), pos));
        }
    }

    private static List<SlotSnapshot> snapshotPositions(List<Storage.Position> positions,
        List<Storage.Position> errors) {
        List<SlotSnapshot> slotSnapshots = new ArrayList<>();
        for (Storage.Position position : positions) {
            snapshotPosition(position, errors, slotSnapshots);
        }
        return slotSnapshots;
    }

    private static void snapshotPosition(Storage.Position position, List<Storage.Position> errors,
        List<SlotSnapshot> slotSnapshots) {
        MinecraftServer server = Utils.getServer();
        ServerLevel level = server.getLevel(position.dimension);
        if (level == null) {
            Mod.LOGGER.warn("Invalid dimension level: {}", position.dimension);
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(position.pos);
        if (!(blockEntity instanceof Container container)) {
            errors.add(position);
            return;
        }

        snapshotOneContainer(container, position, slotSnapshots);
    }

    private static void snapshotOneContainer(Container container, Storage.Position position,
        List<SlotSnapshot> snapshots) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            snapshots.add(new SlotSnapshot(position, i, stack.copy()));
        }
    }

    private static @Nullable BlockPos parseBlockPos(JsonElement element) {
        if (!element.isJsonArray()) {
            return null;
        }
        JsonArray array = element.getAsJsonArray();
        if (array.size() != 3) {
            return null;
        }
        int x;
        int y;
        int z;
        try {
            x = array.get(0).getAsInt();
            y = array.get(1).getAsInt();
            z = array.get(2).getAsInt();
        } catch (Exception e) {
            return null;
        }

        return new BlockPos(x, y, z);
    }

    public record SlotSnapshot(Storage.Position position, int slotIndex, ItemStack stack) {
    }

    public record ConfiguredStorageSnapshots(String fileName, List<SlotSnapshot> snapshots,
        List<Storage.Position> errors) {
        String displayName() {
            return fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - 5) : fileName;
        }
    }
}
