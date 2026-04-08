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

import net.minecraft.core.dispenser.ShulkerBoxDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;

public class DispenserBarteringInit
{
    public static void init()
    {
        DispenserBlock.registerBehavior(
                Items.GOLD_INGOT,
                new DispenserBarterBehavior(
                        1,
                        () -> DispenserBarteringRule.getDispenserBarteringLevel() >= 1
                )
        );
        DispenserBlock.registerBehavior(
                Items.GOLD_BLOCK,
                new DispenserBarterBehavior(
                        9,
                        () -> DispenserBarteringRule.getDispenserBarteringLevel() >= 2
                )
        );

        DispenserBarterShulkerBehavior shulkerBehavior = new DispenserBarterShulkerBehavior(
                () -> DispenserBarteringRule.getDispenserBarteringLevel() >= 3,
                new ShulkerBoxDispenseBehavior()
        );
        DispenserBlock.registerBehavior(Items.SHULKER_BOX, shulkerBehavior);
        for (
            Item item : BuiltInRegistries.ITEM
        )
        {
            Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
            String path = itemId.getPath();
            if (path.endsWith("shulker_box"))
            {
                DispenserBlock.registerBehavior(item, shulkerBehavior);
            }
        }
    }

}
