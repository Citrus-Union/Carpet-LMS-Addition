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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.level.ChunkPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.nm.lms.carpetlmsaddition.rules.minecartchunkloader.MinecartChunkLoader;
import cn.nm.lms.carpetlmsaddition.rules.minecartchunkloader.MinecartChunkLoaderInit;

@Mixin(
    MinecartFurnace.class
)
public abstract class MinecartChunkLoaderMixin
{
    @Shadow
    private int fuel;

    @Inject(
            method = "tick",
            at = @At(
                "TAIL"
            )
    )
    private void minecartChunkLoader(CallbackInfo ci)
    {
        long timeout = MinecartChunkLoader.minecartChunkLoader;
        if (timeout <= 0) return;
        TicketType MINECART = MinecartChunkLoaderInit.getTicket(timeout);
        MinecartFurnace minecart = (MinecartFurnace) (Object) this;
        if (!(minecart.level() instanceof ServerLevel serverLevel)) return;
        if (this.fuel <= 0) return;

        serverLevel.getChunkSource()
                   .addTicketWithRadius(
                           MINECART,
                           //#if MC>=260100
                           ChunkPos.containing(minecart.blockPosition()),
                           //#else
                           //$$ new ChunkPos(minecart.blockPosition()),
                           //#endif
                           3
                   );
    }
}
