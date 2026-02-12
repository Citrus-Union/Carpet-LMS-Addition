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
package cn.nm.lms.carpetlmsaddition.mixin.rule.block.breaking;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.nm.lms.carpetlmsaddition.rule.block.breaking.SoftTrialSpawnerRule;

@Mixin(
    BlockStateBase.class
)
public abstract class SoftTrialSpawnerMixin
{
    @Shadow
    public abstract Block getBlock();

    @Inject(
            method = "getDestroySpeed",
            at = @At(
                "RETURN"
            ),
            cancellable = true
    )
    private void trialSpawnerHardnessTo5$LMS(
            BlockGetter world,
            BlockPos pos,
            CallbackInfoReturnable<Float> cir
    )
    {
        if (SoftTrialSpawnerRule.softTrialSpawner && this.getBlock() == Blocks.TRIAL_SPAWNER)
        {
            cir.setReturnValue(3.0F);
        }
    }
}
