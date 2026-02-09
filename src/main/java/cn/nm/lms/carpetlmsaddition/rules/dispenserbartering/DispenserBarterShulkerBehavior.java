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
package cn.nm.lms.carpetlmsaddition.rules.dispenserbartering;

import java.util.function.BooleanSupplier;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import org.jspecify.annotations.NonNull;

public class DispenserBarterShulkerBehavior extends DispenserBarterBaseBehavior
{
    private final BooleanSupplier enabled;
    private final DispenseItemBehavior fallbackBehavior;

    public DispenserBarterShulkerBehavior(
            BooleanSupplier enabled,
            DispenseItemBehavior fallbackBehavior
    )
    {
        this.enabled = enabled;
        this.fallbackBehavior = fallbackBehavior;
    }

    @Override
    protected @NonNull ItemStack execute(@NonNull BlockSource source, @NonNull ItemStack stack)
    {
        if (!enabled.getAsBoolean() || stack.isEmpty() || hasRequiredName(source))
        {
            return fallbackBehavior.dispense(source, stack);
        }

        int rolls = calculate(stack);
        if (rolls <= 0)
        {
            return fallbackBehavior.dispense(source, stack);
        }

        ItemStack shulkerDrop = stack.copyWithCount(1);
        stack.shrink(1);
        spawnItemFromDispenser(source, shulkerDrop);
        spawnBarterDrops(source, rolls);
        return stack;
    }

    private static int calculate(ItemStack shulkerStack)
    {
        ItemContainerContents container = shulkerStack.get(DataComponents.CONTAINER);
        if (container == null)
        {
            return 0;
        }

        int total = 0;
        for (
            ItemStack inner :
        //#if MC>=260100
        container.nonEmptyItemCopyStream().toList()
        //#else
        //$$ container.nonEmptyItemsCopy()
        //#endif
        )
        {
            if (inner.is(Items.GOLD_INGOT))
            {
                total += inner.getCount();
            }
            else if (inner.is(Items.GOLD_BLOCK))
            {
                total += inner.getCount() * 9;
            }
            else
            {
                return 0;
            }
        }

        return total;
    }
}
