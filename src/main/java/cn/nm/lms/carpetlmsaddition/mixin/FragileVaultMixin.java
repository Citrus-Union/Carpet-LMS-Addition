package cn.nm.lms.carpetlmsaddition.mixin;

import cn.nm.lms.carpetlmsaddition.rules.FragileVault;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExplosionBehavior.class)
public abstract class FragileVaultMixin {
  @ModifyReturnValue(method = "getBlastResistance", at = @At("RETURN"))
  private Optional<Float> carpetlmsaddition$vaultBlastResistanceTo3(
      Optional<Float> original,
      Explosion explosion,
      BlockView world,
      BlockPos pos,
      BlockState blockState,
      FluidState fluidState) {
    if (FragileVault.fragileVault && blockState.isOf(Blocks.VAULT)) {
      return Optional.of(3.0F);
    }

    return original;
  }
}
