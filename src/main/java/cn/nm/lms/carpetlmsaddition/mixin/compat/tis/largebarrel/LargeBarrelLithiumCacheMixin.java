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
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;

import cn.nm.lms.carpetlmsaddition.rule.compat.tis.largebarrel.TisLargeBarrelCompat;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;

//#if MC >= 12105
//$$ import net.minecraft.server.level.ServerLevel;
//#endif

/**
 * Port of TIS Addition's lithium cache reset for MC &gt;= 1.20.5, where the upstream mixin is currently a stub.
 */
@Restriction(require = {@Condition("carpet-tis-addition"), @Condition("lithium")})
@Mixin(value = BarrelBlock.class, priority = 2100)
public abstract class LargeBarrelLithiumCacheMixin extends BaseEntityBlock {
    protected LargeBarrelLithiumCacheMixin(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, world, pos, oldState, moved);
        if (shouldResetLithiumHopperCache()) {
            resetLithiumHopperCache(world, pos, state);
        }
    }

    @ModifyVariable(
        //#if MC >= 12105
        //$$ method = "affectNeighborsAfterRemoval",
        //#else
        method = "onRemove",
        //#endif
        at = @At("TAIL"), argsOnly = true)
    //#if MC >= 12105
    //$$ private ServerLevel lms$resetLithiumHopperCacheOnRemove(ServerLevel world,
    //#else
    private Level lms$resetLithiumHopperCacheOnRemove(Level world,
    //#endif
        @Local(argsOnly = true, ordinal = 0) BlockState state, @Local(argsOnly = true) BlockPos pos) {
        if (shouldResetLithiumHopperCache()) {
            resetLithiumHopperCache(world, pos, state);
        }
        return world;
    }

    @Unique
    private static boolean shouldResetLithiumHopperCache() {
        return largeBarrelFix && TisLargeBarrelCompat.isAvailable() && TisLargeBarrelCompat.isTisLargeBarrelEnabled();
    }

    @Unique
    private static void resetLithiumHopperCache(LevelAccessor world, BlockPos changedBarrelPos,
        BlockState changedBarrelState) {
        if (world.isClientSide()) {
            return;
        }

        Direction changedBarrelDirection = changedBarrelState.getValue(BarrelBlock.FACING);
        BlockPos affectedBarrelPos = changedBarrelPos.relative(changedBarrelDirection.getOpposite());
        BlockState affectedBarrelState = world.getBlockState(affectedBarrelPos);
        if (affectedBarrelState.getBlock() instanceof BarrelBlock
            && affectedBarrelState.getValue(BarrelBlock.FACING) == changedBarrelDirection.getOpposite()) {
            BlockEntity barrelBlockEntity = world.getBlockEntity(affectedBarrelPos);
            if (barrelBlockEntity instanceof BarrelBlockEntity) {
                TisLargeBarrelCompat.invalidateLithiumHopperCache(barrelBlockEntity);
            }
        }
    }
}
