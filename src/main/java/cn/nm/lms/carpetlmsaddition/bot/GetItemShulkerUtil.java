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
package cn.nm.lms.carpetlmsaddition.bot;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import cn.nm.lms.carpetlmsaddition.lib.Utils;

final class GetItemShulkerUtil {
    private GetItemShulkerUtil() {}

    static boolean isShulker(Item item) {
        return Utils.isShulkerBox(new ItemStack(item));
    }

    static int slotAmount(ItemStack stack, Item target, boolean targetIsShulker) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        if (targetIsShulker) {
            if (!Utils.isShulkerBox(stack)) {
                return 0;
            }
            return boxInBox(stack);
        }
        if (stack.getItem() == target) {
            return stack.getCount();
        }
        if (!Utils.isShulkerBox(stack)) {
            return 0;
        }
        return itemInBox(stack, target);
    }

    static int itemInBox(ItemStack box, Item target) {
        if (isShulker(target)) {
            return 0;
        }

        int perBox = itemInOne(box.copyWithCount(1), target);
        if (perBox <= 0) {
            return 0;
        }
        return perBox * box.getCount();
    }

    static int boxInBox(ItemStack box) {
        int perBox = boxInOne(box.copyWithCount(1));
        if (perBox <= 0) {
            return 0;
        }
        return perBox * box.getCount();
    }

    private static int itemInOne(ItemStack box, Item target) {
        if (!Utils.isShulkerBox(box)) {
            return -1;
        }
        ItemContainerContents container = box.get(DataComponents.CONTAINER);
        if (container == null) {
            return 0;
        }

        int total = 0;
        for (ItemStack inner : Utils.nonItemCopyList(container)) {
            if (inner.isEmpty()) {
                continue;
            }
            if (inner.getItem() == target) {
                total += inner.getCount();
                continue;
            }
            if (!Utils.isShulkerBox(inner)) {
                return -1;
            }
            int nested = itemInOne(inner.copyWithCount(1), target);
            if (nested < 0) {
                return -1;
            }
            total += nested * inner.getCount();
        }
        return total;
    }

    private static int boxInOne(ItemStack box) {
        if (!Utils.isShulkerBox(box)) {
            return -1;
        }
        ItemContainerContents container = box.get(DataComponents.CONTAINER);
        if (container == null) {
            return 1;
        }

        int total = 1;
        for (ItemStack inner : Utils.nonItemCopyList(container)) {
            if (inner.isEmpty()) {
                continue;
            }
            if (!Utils.isShulkerBox(inner)) {
                return -1;
            }
            int nested = boxInOne(inner.copyWithCount(1));
            if (nested < 0) {
                return -1;
            }
            total += nested * inner.getCount();
        }
        return total;
    }
}
