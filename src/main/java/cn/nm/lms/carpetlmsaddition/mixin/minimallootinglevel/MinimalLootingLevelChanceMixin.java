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
package cn.nm.lms.carpetlmsaddition.mixin.minimallootinglevel;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import cn.nm.lms.carpetlmsaddition.rules.minimallootinglevel.MinimalLootingLevelLib;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;

@Mixin(LootItemRandomChanceWithEnchantedBonusCondition.class)
public abstract class MinimalLootingLevelChanceMixin
{
    @Shadow
    @Final
    private LevelBasedValue enchantedChance;
    @Shadow
    @Final
    private Holder<Enchantment> enchantment;

    @ModifyReturnValue(
            method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", at = @At("RETURN"))
    private boolean minimumLootingLevel(boolean result, LootContext context)
    {
        int level = MinimalLootingLevelLib.getLootingLevel(context, enchantment);
        if (!enchantment.is(Enchantments.LOOTING))
        {
            return result;
        }

        int effectiveLevel = MinimalLootingLevelLib.effectiveLootingLevel(level);
        float chance = enchantedChance.calculate(effectiveLevel);
        return context.getRandom().nextFloat() < chance;
    }
}
