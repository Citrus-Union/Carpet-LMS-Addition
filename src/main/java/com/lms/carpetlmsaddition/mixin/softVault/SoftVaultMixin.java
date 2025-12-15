package com.lms.carpetlmsaddition.mixin.softVault;

import com.lms.carpetlmsaddition.rules.softVault.SoftVaultRuleSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(value = BlockBehaviour.BlockStateBase.class)
public abstract class SoftVaultMixin {
    @Shadow public abstract Block getBlock();

    @Inject(method = "getDestroySpeed",at = @At(value = "HEAD"),cancellable = true)
    public void lms$softVaultHardnessOverride(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Float> cir){

        if (!SoftVaultRuleSettings.softVault) {
            return;
        }
        var block = this.getBlock();
        if(block instanceof VaultBlock || block instanceof TrialSpawnerBlock){
            cir.setReturnValue(3.0F);
            cir.cancel();
        }
    }
}
