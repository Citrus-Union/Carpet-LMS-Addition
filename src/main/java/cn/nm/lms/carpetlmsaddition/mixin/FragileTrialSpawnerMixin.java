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
package cn.nm.lms.carpetlmsaddition.mixin;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.nm.lms.carpetlmsaddition.rules.FragileTrialSpawner;

@Mixin(
    ExplosionDamageCalculator.class
)
public abstract class FragileTrialSpawnerMixin
{
    @Inject(
            method = "getBlockExplosionResistance",
            at = @At(
                "HEAD"
            ),
            cancellable = true
    )
    private void trialSpawnerBlastResistanceTo3$LMS(
            Explosion explosion,
            BlockGetter world,
            BlockPos pos,
            BlockState blockState,
            FluidState fluidState,
            CallbackInfoReturnable<Optional<Float>> cir
    )
    {
        if (FragileTrialSpawner.fragileTrialSpawner && blockState.is(Blocks.TRIAL_SPAWNER))
        {
            cir.setReturnValue(Optional.of(3.0F));
        }
    }
}
