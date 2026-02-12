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
package cn.nm.lms.carpetlmsaddition.rule.block.util.dispenserbartering;

import java.util.function.BooleanSupplier;

import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.NonNull;

public class DispenserBarterBehavior extends DispenserBarterBaseBehavior
{
    private final int rolls;
    private final BooleanSupplier enabled;

    public DispenserBarterBehavior(int rolls, BooleanSupplier enabled)
    {
        this.rolls = rolls;
        this.enabled = enabled;
    }

    @Override
    protected @NonNull ItemStack execute(@NonNull BlockSource source, @NonNull ItemStack stack)
    {
        if (!enabled.getAsBoolean() || stack.isEmpty() || hasRequiredName(source))
        {
            return super.execute(source, stack);
        }

        stack.shrink(1);
        spawnBarterDrops(source, rolls);
        return stack;
    }
}
