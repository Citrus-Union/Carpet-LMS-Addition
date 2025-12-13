package com.lms.carpetlmsaddition.mixin.fragileTrialSpawners;

import com.lms.carpetlmsaddition.rules.fragileTrialSpawners.FragileTrialSpawnerHelper;
import com.lms.carpetlmsaddition.rules.fragileTrialSpawners.FragileTrialSpawnerRuleSettings;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class FragileTrialSpawnerMixin {
  @Inject(method = "getExplosionResistance", at = @At("HEAD"), cancellable = true)
  private void lms$weakenTrialSpawners(CallbackInfoReturnable<Float> cir) {
    Block self = (Block) (Object) this;
    if (FragileTrialSpawnerRuleSettings.fragileTrialSpawners
        && FragileTrialSpawnerHelper.isTrialSpawner(self)) {
      cir.setReturnValue(FragileTrialSpawnerHelper.SPAWNER_BLAST_RESISTANCE);
    }
  }
}
