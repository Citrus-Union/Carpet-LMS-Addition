package cn.nm.lms.carpetlmsaddition.mixin.minimallootinglevel;

import cn.nm.lms.carpetlmsaddition.rules.minimallootinglevel.MinimalLootingLevelLib;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantedCountIncreaseFunction.class)
public abstract class MinimalLootingLevelCountMixin {
  @Shadow @Final private Holder<Enchantment> enchantment;
  @Shadow @Final private NumberProvider value;
  @Shadow @Final private int limit;

  @ModifyReturnValue(
      method =
          "run(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/storage/loot/LootContext;)Lnet/minecraft/world/item/ItemStack;",
      at = @At("RETURN"))
  private ItemStack minimumLootingLevel(ItemStack result, ItemStack stack, LootContext context) {
    int level = MinimalLootingLevelLib.getLootingLevel(context, enchantment);
    if (!enchantment.is(Enchantments.LOOTING)) {
      return result;
    }

    float multiplier = value.getFloat(context);
    int effectiveLevel = MinimalLootingLevelLib.effectiveLootingLevel(level);
    int bonus = Math.round(effectiveLevel * multiplier);
    result.setCount(stack.getCount() + bonus);
    if (limit > 0) {
      result.limitSize(limit);
    }
    return result;
  }
}
