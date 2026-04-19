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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import carpet.CarpetServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cn.nm.lms.carpetlmsaddition.Mod;
import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;
import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.lib.getvalue.GetPaths;

public class Storage {
    static final Path configJsonPath = GetPaths.getLmsWorldPath().resolve("checkStorageConfig.json");
    static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    static final Map<String, ResourceKey<Level>> stringToDimension =
        Map.of("end", Level.END, "overworld", Level.OVERWORLD, "nether", Level.NETHER);
    static final Map<ResourceKey<Level>, String> dimensionToSting =
        Map.of(Level.END, "end", Level.OVERWORLD, "overworld", Level.NETHER, "nether");
    static final Path storageDir = GetPaths.getLmsWorldPath().resolve("checkStorageList");
    static final Path storageDataPath = GetPaths.getLmsWorldDataPath().resolve("checkStorageData");

    public static String checkStorage() {
        MinecraftServer server = CarpetServer.minecraft_server;
        return Utils.runOnServerThread(server, () -> doCheckStorage(server));
    }

    public static List<ContainerSnapshot> collectConfiguredContainerSnapshots() {
        List<ContainerSnapshot> snapshots = new ArrayList<>();
        for (ConfiguredStorageSnapshots storageSnapshots : collectConfiguredStorageSnapshots()) {
            snapshots.addAll(storageSnapshots.snapshots);
        }
        return snapshots;
    }

    public static List<ConfiguredStorageSnapshots> collectConfiguredStorageSnapshots() {
        MinecraftServer server = CarpetServer.minecraft_server;
        return Utils.runOnServerThread(server, () -> doCollectConfiguredStorageSnapshots(server));
    }

    private static String doCheckStorage(MinecraftServer server) {
        Count count = new Count();
        List<ConfiguredStorageSnapshots> storages = doCollectConfiguredStorageSnapshots(server);
        count.total = storages.size();

        for (ConfiguredStorageSnapshots storage : storages) {
            try {
                HashMap<Item, ItemCount> items = new HashMap<>();
                StorageItemStackProcessor.processSnapshots(storage.snapshots, items);
                Path savePath = storageDataPath.resolve(storage.fileName);
                StorageJsonService.saveToFile(savePath, items, storage.errors);
                count.success++;
            } catch (Exception e) {
                Mod.LOGGER.warn("Failed to process storage file: {}", storage.fileName);
            }
        }

        return count.praseResult();
    }

    private static List<ConfiguredStorageSnapshots> doCollectConfiguredStorageSnapshots(MinecraftServer server) {
        PreparedInputs prepared = AsyncTasks.supply(StorageJsonService::prepareInputs).join();
        List<ConfiguredStorageSnapshots> storages = new ArrayList<>();
        for (PreparedStorage storageInput : prepared.inputs) {
            List<Position> errors = new ArrayList<>();
            try {
                List<ContainerSnapshot> snapshots = collectSnapshots(storageInput, server, errors);
                storages.add(new ConfiguredStorageSnapshots(storageInput.fileName, snapshots, errors));
            } catch (Exception e) {
                Mod.LOGGER.warn("Failed to collect snapshots from storage file: {}", storageInput.fileName);
            }
        }
        return storages;
    }

    private static List<ContainerSnapshot> collectSnapshots(PreparedStorage storageInput, MinecraftServer server,
        List<Position> errors) {
        return StorageContainerReader.collectSnapshots(storageInput.pos, server, errors);
    }

    public static final class Position {
        public final ResourceKey<Level> dimension;
        public final BlockPos pos;

        Position(ResourceKey<Level> dimension, BlockPos pos) {
            this.dimension = dimension;
            this.pos = pos;
        }

        JsonObject toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("dimension", dimensionToSting.get(this.dimension));
            JsonObject posObject = new JsonObject();
            posObject.addProperty("x", this.pos.getX());
            posObject.addProperty("y", this.pos.getY());
            posObject.addProperty("z", this.pos.getZ());
            jsonObject.add("pos", posObject);
            return jsonObject;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Position other)) {
                return false;
            }
            return Objects.equals(dimension, other.dimension) && Objects.equals(pos, other.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dimension, pos);
        }
    }

    public static final class ContainerSnapshot {
        public final Position position;
        public final List<ItemStack> stacks;

        ContainerSnapshot(Position position, List<ItemStack> stacks) {
            this.position = position;
            this.stacks = stacks;
        }

    }

    public static final class ConfiguredStorageSnapshots {
        final String fileName;
        final List<ContainerSnapshot> snapshots;
        final List<Position> errors;

        ConfiguredStorageSnapshots(String fileName, List<ContainerSnapshot> snapshots, List<Position> errors) {
            this.fileName = fileName;
            this.snapshots = snapshots;
            this.errors = errors;
        }
    }

    static final class PreparedStorage {
        final String fileName;
        final JsonObject pos;

        PreparedStorage(String fileName, JsonObject pos) {
            this.fileName = fileName;
            this.pos = pos;
        }
    }

    static final class PreparedInputs {
        static final PreparedInputs EMPTY = new PreparedInputs(0, List.of());
        final int total;
        final List<PreparedStorage> inputs;

        PreparedInputs(int total, List<PreparedStorage> inputs) {
            this.total = total;
            this.inputs = inputs;
        }
    }

    static final class Count {
        int success = 0;
        int total = 0;

        String praseResult() {
            return String.format("CheckStorage finished, success: %d, total: %d", this.success, this.total);
        }
    }

    static final class ItemCount {
        int count = 0;
        HashMap<Position, Integer> positionsCount = new HashMap<>();

        synchronized void addItemStack(ItemStack itemStack, int times, Position position) {
            int increaseCount = itemStack.getCount() * times;
            this.count += increaseCount;
            int newPositionCount = this.positionsCount.getOrDefault(position, 0) + increaseCount;
            this.positionsCount.put(position, newPositionCount);
        }
    }
}
