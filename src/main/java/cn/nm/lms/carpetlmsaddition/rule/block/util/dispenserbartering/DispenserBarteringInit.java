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
        DispenserBlock.registerBehavior(Items.WHITE_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.ORANGE_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.MAGENTA_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.LIGHT_BLUE_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.YELLOW_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.LIME_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.PINK_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.GRAY_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.LIGHT_GRAY_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.CYAN_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.PURPLE_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.BLUE_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.BROWN_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.GREEN_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.RED_SHULKER_BOX, shulkerBehavior);
        DispenserBlock.registerBehavior(Items.BLACK_SHULKER_BOX, shulkerBehavior);
    }

}
