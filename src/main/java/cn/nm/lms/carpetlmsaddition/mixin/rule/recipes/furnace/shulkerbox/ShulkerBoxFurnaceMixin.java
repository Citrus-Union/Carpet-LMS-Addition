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
package cn.nm.lms.carpetlmsaddition.mixin.rule.recipes.furnace.shulkerbox;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.nm.lms.carpetlmsaddition.rule.recipes.furnace.shulkerbox.ShulkerBoxFurnaceService;

@Mixin(
    AbstractFurnaceBlockEntity.class
)
public abstract class ShulkerBoxFurnaceMixin
{
    @Inject(
            method = "serverTick",
            at = @At(
                "HEAD"
            ),
            cancellable = true
    )
    private static void shulkerBoxFurnace$lms(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            AbstractFurnaceBlockEntity entity,
            CallbackInfo ci
    )
    {
        if (ShulkerBoxFurnaceService.tryTick(level, pos, state, entity))
        {
            ci.cancel();
        }
    }
}
