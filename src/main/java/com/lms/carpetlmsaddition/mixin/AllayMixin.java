package com.lms.carpetlmsaddition.mixin;

import com.lms.carpetlmsaddition.CarpetLmsSettings;
import net.minecraft.world.entity.animal.allay.Allay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Allay.class)
public abstract class AllayMixin {
  @ModifyConstant(method = "aiStep", constant = @Constant(intValue = 10))
  private int lms$customHealInterval(int interval) {
    // Allow configuring how frequently Allays passively heal server-side.
    return CarpetLmsSettings.allayHealInterval;
  }
}
