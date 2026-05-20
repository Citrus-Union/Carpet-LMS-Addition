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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.Item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cn.nm.lms.carpetlmsaddition.Mod;

final class StorageDataCollector {
    private static Map<Item, Storage.ItemCount> countSnapshots(List<StorageContainerReader.SlotSnapshot> snapshots) {
        Map<Item, Storage.ItemCount> items = new HashMap<>();
        for (StorageContainerReader.SlotSnapshot snapshot : snapshots) {
            StorageItemStackProcessor.processStack(snapshot.stack(), items);
        }
        return items;
    }

    JsonArray generateStorageDataJson() {
        JsonArray response = new JsonArray();
        for (StorageContainerReader.ConfiguredStorageSnapshots snapshots : StorageContainerReader
            .collectConfiguredStorageSnapshots()) {
            try {
                Map<Item, Storage.ItemCount> items = countSnapshots(snapshots.snapshots());
                JsonObject oneStorage = new JsonObject();
                oneStorage.addProperty("n", snapshots.displayName());
                oneStorage.add("d", StorageJsonService.toOutputItemsArray(items));
                oneStorage.add("e", StorageJsonService.toOutputErrorsObject(snapshots.errors()));
                response.add(oneStorage);
            } catch (Exception e) {
                Mod.LOGGER.warn("Failed to process storage file: {}", snapshots.fileName(), e);
            }
        }
        return response;
    }

    Map<Item, Integer> generateStorageItemCounts() {
        HashMap<Item, Integer> counts = new HashMap<>();
        for (StorageContainerReader.ConfiguredStorageSnapshots snapshots : StorageContainerReader
            .collectConfiguredStorageSnapshots()) {
            try {
                Map<Item, Storage.ItemCount> items = countSnapshots(snapshots.snapshots());
                items.forEach((item, itemCount) -> {
                    counts.put(item, counts.getOrDefault(item, 0) + itemCount.count);
                });
            } catch (Exception e) {
                Mod.LOGGER.warn("Failed to process storage file: {}", snapshots.fileName(), e);
            }
        }
        return counts;
    }
}
