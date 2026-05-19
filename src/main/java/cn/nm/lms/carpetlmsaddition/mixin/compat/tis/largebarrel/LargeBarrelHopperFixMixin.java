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
package cn.nm.lms.carpetlmsaddition.mixin.compat.tis.largebarrel;

import static cn.nm.lms.carpetlmsaddition.rule.Settings.largeBarrelFix;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import cn.nm.lms.carpetlmsaddition.rule.compat.tis.largebarrel.TisLargeBarrelCompat;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;

/**
 * Carpet TIS Addition injects {@code getBlockContainer} for MC &gt;= 1.20.5, but the mixin can fail to
 * replace the inventory (for example when {@code @Local} capture does not match). This mixin re-applies the
 * large barrel inventory lookup with a higher priority.
 */
@Restriction(require = @Condition("carpet-tis-addition"))
@Mixin(value = HopperBlockEntity.class, priority = 2100)
public abstract class LargeBarrelHopperFixMixin {
    //#if MC >= 12005
    @Inject(method = "getBlockContainer",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"),
        cancellable = true)
    //#else
    //$$ @Inject(
    //$$     method = "getContainerAt(Lnet/minecraft/world/level/Level;DDD)Lnet/minecraft/world/Container;",
    //$$     at = @At(value = "INVOKE",
    //$$         target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"),
    //$$     cancellable = true)
    //#endif
    private static void lms$useLargeBarrelInventoryForHopper(CallbackInfoReturnable<Container> cir,
        @Local(argsOnly = true) Level world, @Local(argsOnly = true) BlockPos pos,
        @Local(argsOnly = true) BlockState state) {
        if (!largeBarrelFix || !TisLargeBarrelCompat.isTisLargeBarrelEnabled()) {
            return;
        }
        if (state.getBlock() instanceof BarrelBlock) {
            Container largeBarrel = TisLargeBarrelCompat.getLargeBarrelInventory(state, world, pos);
            if (largeBarrel != null) {
                cir.setReturnValue(largeBarrel);
            }
        }
    }
}
