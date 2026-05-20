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

import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import cn.nm.lms.carpetlmsaddition.storage.StorageSlotCounter;
import cn.nm.lms.carpetlmsaddition.storage.StorageSlotCounter.Result;

final class StorageItemStackProcessor {
    private final Map<Item, Storage.ItemCount> items;

    private StorageItemStackProcessor(Map<Item, Storage.ItemCount> items) {
        this.items = items;
    }

    static void processStack(ItemStack stack, Map<Item, Storage.ItemCount> items) {
        StorageItemStackProcessor processor = new StorageItemStackProcessor(items);
        processor.addSlot(stack);
    }

    private void addSlot(ItemStack itemStack) {
        Result result = StorageSlotCounter.count(itemStack);
        if (result == null) {
            return;
        }
        Storage.ItemCount itemCount = this.items.computeIfAbsent(result.item(), ignored -> new Storage.ItemCount());
        itemCount.add(result.count());
    }
}
