package cn.nm.lms.carpetlmsaddition.rules.minimallootinglevel

import net.minecraft.core.Holder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import kotlin.math.max

object MinimalLootingLevelLib {
    @JvmStatic
    fun getLootingLevel(
        context: LootContext,
        enchantment: Holder<Enchantment>,
    ): Int {
        val attacker = context.getOptionalParameter(LootContextParams.ATTACKING_ENTITY)
        if (attacker is LivingEntity) {
            return EnchantmentHelper.getEnchantmentLevel(enchantment, attacker)
        }
        return 0
    }

    @JvmStatic
    fun effectiveLootingLevel(currentLevel: Int): Int = max(currentLevel, MinimalLootingLevel.minimalLootingLevel)
}
