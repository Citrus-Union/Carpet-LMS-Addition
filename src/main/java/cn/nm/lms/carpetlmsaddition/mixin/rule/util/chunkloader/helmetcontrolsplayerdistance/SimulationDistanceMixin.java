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
package cn.nm.lms.carpetlmsaddition.mixin.rule.util.chunkloader.helmetcontrolsplayerdistance;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.Ticket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cn.nm.lms.carpetlmsaddition.lib.getvalue.HelmetLoadValue;

@Mixin(
    DistanceManager.class
)
public abstract class SimulationDistanceMixin
{
    @Unique
    private static final Map<UUID, Integer> lastSimTicketLevel = new ConcurrentHashMap<>();

    @Shadow
    private int simulationDistance;

    @Redirect(
            method = "addPlayer(Lnet/minecraft/core/SectionPos;Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/TicketStorage;addTicket(Lnet/minecraft/server/level/Ticket;Lnet/minecraft/world/level/ChunkPos;)V"
            )
    )
    private void applyHelmetSimulationDistanceOnAdd$LMS(
            TicketStorage ticketStorage,
            Ticket ticket,
            ChunkPos chunkPos,
            SectionPos sectionPos,
            ServerPlayer serverPlayer
    )
    {
        Ticket adjustedTicket = this.createSimulationTicket$LMS(ticket, serverPlayer);
        lastSimTicketLevel.put(serverPlayer.getUUID(), adjustedTicket.getTicketLevel());
        ticketStorage.addTicket(adjustedTicket, chunkPos);
    }

    @Redirect(
            method = "removePlayer(Lnet/minecraft/core/SectionPos;Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/TicketStorage;removeTicket(Lnet/minecraft/server/level/Ticket;Lnet/minecraft/world/level/ChunkPos;)V"
            )
    )
    private void applyHelmetSimulationDistanceOnRemove$LMS(
            TicketStorage ticketStorage,
            Ticket ticket,
            ChunkPos chunkPos,
            SectionPos sectionPos,
            ServerPlayer serverPlayer
    )
    {
        Ticket adjustedTicket = this.createSimulationTicket$LMS(ticket, serverPlayer);
        long chunkKey$LMS = this.chunkKey$LMS(chunkPos);

        Integer storedLevel = lastSimTicketLevel.get(serverPlayer.getUUID());

        if (storedLevel != null)
        {
            Ticket storedTicket = new Ticket(ticket.getType(), storedLevel);
            if (ticketStorage.removeTicket(chunkKey$LMS, storedTicket))
            {
                lastSimTicketLevel.remove(serverPlayer.getUUID());
                return;
            }
        }

        if (ticketStorage.removeTicket(chunkKey$LMS, adjustedTicket))
        {
            lastSimTicketLevel.remove(serverPlayer.getUUID());
            return;
        }

        if (adjustedTicket != ticket)
        {
            ticketStorage.removeTicket(chunkKey$LMS, ticket);
        }
    }

    @Unique
    private Ticket createSimulationTicket$LMS(Ticket originalTicket, ServerPlayer player)
    {
        int helmetDistance = HelmetLoadValue.helmetLoadValue(player);
        if (helmetDistance <= 0)
        {
            return originalTicket;
        }

        int cappedDistance = Math.min(helmetDistance, this.simulationDistance);
        int level = Math.max(
                0,
                ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING) - cappedDistance
        );
        if (level == originalTicket.getTicketLevel())
        {
            return originalTicket;
        }

        return new Ticket(originalTicket.getType(), level);
    }

    @Unique
    private long chunkKey$LMS(ChunkPos chunkPos)
    {
        //#if MC>=260100
        return chunkPos.pack();
        //#else
        //$$ return chunkPos.toLong();
        //#endif
    }
}
