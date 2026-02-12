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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import cn.nm.lms.carpetlmsaddition.lib.check.CheckName;

public abstract class DispenserBarterBaseBehavior extends DefaultDispenseItemBehavior
{
    private static final ResourceKey<LootTable> PIGLIN_BARTER_TABLE = ResourceKey.create(
            Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath("minecraft", "gameplay/piglin_bartering")
    );

    protected final void spawnBarterDrops(BlockSource source, int rolls)
    {
        if (rolls <= 0)
        {
            return;
        }

        ServerLevel level = source.level();
        LootTable table = level.getServer()
                               .reloadableRegistries()
                               .getLootTable(PIGLIN_BARTER_TABLE);
        LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        List<AccumulatedDrop> accumulatedDrops = new ArrayList<>();

        for (
                int i = 0;
                i < rolls;
                i++
        )
        {
            List<ItemStack> drops = table.getRandomItems(params);
            for (
                ItemStack drop : drops
            )
            {
                mergeDrop(accumulatedDrops, drop);
            }
        }

        Direction facing = source.state().getValue(DispenserBlock.FACING);
        Position dispensePosition = DispenserBlock.getDispensePosition(source);
        for (
            AccumulatedDrop accumulatedDrop : accumulatedDrops
        )
        {
            long remaining = accumulatedDrop.count;
            int maxStackSize = accumulatedDrop.template.getMaxStackSize();
            while (remaining > 0)
            {
                int split = (int) Math.min(remaining, maxStackSize);
                ItemStack output = accumulatedDrop.template.copyWithCount(split);
                DefaultDispenseItemBehavior.spawnItem(level, output, 6, facing, dispensePosition);
                remaining -= split;
            }
        }
    }

    protected final void spawnItemFromDispenser(BlockSource source, ItemStack stack)
    {
        Direction facing = source.state().getValue(DispenserBlock.FACING);
        Position dispensePosition = DispenserBlock.getDispensePosition(source);
        DefaultDispenseItemBehavior.spawnItem(source.level(), stack, 6, facing, dispensePosition);
    }

    protected final boolean hasRequiredName(BlockSource source)
    {
        String name = source.blockEntity().getName().getString();
        return !CheckName.checkName(
                name,
                DispenserBarteringRule.dispenserBarteringName,
                "bartering"
        );
    }

    private static void mergeDrop(List<AccumulatedDrop> accumulatedDrops, ItemStack drop)
    {
        if (drop.isEmpty())
        {
            return;
        }

        for (
            AccumulatedDrop accumulatedDrop : accumulatedDrops
        )
        {
            if (ItemStack.isSameItemSameComponents(accumulatedDrop.template, drop))
            {
                accumulatedDrop.count += drop.getCount();
                return;
            }
        }

        accumulatedDrops.add(new AccumulatedDrop(drop.copyWithCount(1), drop.getCount()));
    }

    private static final class AccumulatedDrop
    {
        private final ItemStack template;
        private long count;

        private AccumulatedDrop(ItemStack template, long count)
        {
            this.template = template;
            this.count = count;
        }
    }
}
