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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

@Mixin(EnchantedCountIncreaseFunction.class)
public abstract class MinimalLootingLevelCountMixin
{
    @Shadow
    @Final
    private Holder<Enchantment> enchantment;
    @Shadow
    @Final
    private NumberProvider count;
    @Shadow
    @Final
    private int limit;

    @ModifyReturnValue(
            method = "run(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/storage/loot/LootContext;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
    private ItemStack minimumLootingLevel(ItemStack result, ItemStack stack, LootContext context)
    {
        int level = MinimalLootingLevelLib.getLootingLevel(context, enchantment);
        if (!enchantment.is(Enchantments.LOOTING))
        {
            return result;
        }

        float multiplier = this.count.getFloat(context);
        int effectiveLevel = MinimalLootingLevelLib.effectiveLootingLevel(level);
        int bonus = Math.round(effectiveLevel * multiplier);
        result.setCount(stack.getCount() + bonus);
        if (limit > 0)
        {
            result.limitSize(limit);
        }
        return result;
    }
}
