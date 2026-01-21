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
package cn.nm.lms.carpetlmsaddition.mixin.helmetcontrolsplayerdistance;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.nm.lms.carpetlmsaddition.lib.getvalue.HelmetLoadValue;

@Mixin(
    ChunkMap.class
)
public abstract class ViewDistanceMixin
{

    @Inject(
            method = "getPlayerViewDistance(Lnet/minecraft/server/level/ServerPlayer;)I",
            at = @At(
                "RETURN"
            ),
            cancellable = true
    )
    private void viewDistance(ServerPlayer player, CallbackInfoReturnable<Integer> cir)
    {
        int n = HelmetLoadValue.helmetLoadValue(player);
        if (n > 0)
        {
            int defaultViewDistance = cir.getReturnValue();

            n = Math.min(n, defaultViewDistance);
            cir.setReturnValue(n);
        }
    }

}
