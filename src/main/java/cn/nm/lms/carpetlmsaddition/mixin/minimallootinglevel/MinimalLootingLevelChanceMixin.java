package cn.nm.lms.carpetlmsaddition.mixin.minimallootinglevel;

import cn.nm.lms.carpetlmsaddition.rules.minimallootinglevel.MinimalLootingLevelLib;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootItemRandomChanceWithEnchantedBonusCondition.class)
public abstract class MinimalLootingLevelChanceMixin {
  @Shadow @Final private float unenchantedChance;
  @Shadow @Final private LevelBasedValue enchantedChance;
  @Shadow @Final private Holder<Enchantment> enchantment;

  @ModifyReturnValue(
      method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z",
      at = @At("RETURN"))
  private boolean minimumLootingLevel(boolean result, LootContext context) {
    int level = MinimalLootingLevelLib.getLootingLevel(context, enchantment);
    if (!enchantment.is(Enchantments.LOOTING)) {
      return result;
    }

    int effectiveLevel = MinimalLootingLevelLib.effectiveLootingLevel(level);
    float chance = enchantedChance.calculate(effectiveLevel);
    return context.getRandom().nextFloat() < chance;
  }
}
