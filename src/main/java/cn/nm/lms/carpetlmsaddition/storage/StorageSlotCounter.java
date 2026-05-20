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
package cn.nm.lms.carpetlmsaddition.storage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.lib.Utils;

public final class StorageSlotCounter {
    private StorageSlotCounter() {}

    public static @Nullable Result count(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        if (!Utils.isShulkerBox(stack)) {
            return new Result(stack.getItem(), stack.getCount(), true);
        }

        ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        if (container == null) {
            return new Result(Items.SHULKER_BOX, stack.getCount(), false);
        }

        Result merged = null;
        for (ItemStack inner : Utils.nonItemCopyList(container)) {
            Result innerResult = count(inner);
            if (innerResult == null) {
                continue;
            }
            if (merged != null && merged.item() != innerResult.item()) {
                return null;
            }
            merged = new Result(innerResult.item(), (merged == null ? 0 : merged.count()) + innerResult.count(), false);
        }

        if (merged == null) {
            return new Result(Items.SHULKER_BOX, stack.getCount(), false);
        }
        if (merged.item() == Items.SHULKER_BOX) {
            return new Result(Items.SHULKER_BOX, (merged.count() + 1) * stack.getCount(), false);
        }
        return new Result(merged.item(), merged.count() * stack.getCount(), false);
    }

    public static Item normalize(Item item) {
        return Utils.isShulkerBox(item) ? Items.SHULKER_BOX : item;
    }

    public record Result(Item item, int count, boolean noShulkerBox) {
        public boolean matches(Item target) {
            return this.item == target && this.count > 0;
        }
    }
}
