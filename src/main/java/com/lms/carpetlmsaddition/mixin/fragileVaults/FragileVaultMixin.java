package com.lms.carpetlmsaddition.mixin.fragileVaults;

import com.lms.carpetlmsaddition.rules.fragileVaults.FragileVaultHelper;
import com.lms.carpetlmsaddition.rules.fragileVaults.FragileVaultRuleSettings;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class FragileVaultMixin {
  @Inject(method = "getExplosionResistance", at = @At("HEAD"), cancellable = true)
  private void lms$weakenVaults(CallbackInfoReturnable<Float> cir) {
    Block self = (Block) (Object) this;
    if (FragileVaultRuleSettings.fragileVaults && FragileVaultHelper.isVault(self)) {
      cir.setReturnValue(FragileVaultHelper.BEACON_BLAST_RESISTANCE);
    }
  }
}
