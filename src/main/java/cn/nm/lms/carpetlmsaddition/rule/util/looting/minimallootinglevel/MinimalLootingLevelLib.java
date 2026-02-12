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
package cn.nm.lms.carpetlmsaddition.rule.util.looting.minimallootinglevel;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class MinimalLootingLevelLib
{

    public static int getLootingLevel(LootContext context, Holder<Enchantment> enchantment)
    {
        Object attacker = context.getOptionalParameter(LootContextParams.ATTACKING_ENTITY);
        if (attacker instanceof LivingEntity)
        {
            return EnchantmentHelper.getEnchantmentLevel(enchantment, (LivingEntity) attacker);
        }
        return 0;
    }

    public static int effectiveLootingLevel(int currentLevel)
    {
        return Math.max(currentLevel, MinimalLootingLevelRule.minimalLootingLevel);
    }
}
