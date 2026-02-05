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
package cn.nm.lms.carpetlmsaddition.mixin;

import net.minecraft.world.entity.monster.Shulker;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import cn.nm.lms.carpetlmsaddition.rules.ShulkerDupLowHealthFailChance;

@Mixin(
    Shulker.class
)
public abstract class ShulkerDupLowHealthFailChanceMixin
{
    @ModifyConstant(
            method = "hurtServer",
            constant = @Constant(
                    intValue = 4,
                    ordinal = 0
            )
    )
    private int changeBlacklistLimit(int original)
    {
        int chance = ShulkerDupLowHealthFailChance.shulkerDupLowHealthFailChance;
        return chance == 0 ? Integer.MAX_VALUE : chance;
    }
}
