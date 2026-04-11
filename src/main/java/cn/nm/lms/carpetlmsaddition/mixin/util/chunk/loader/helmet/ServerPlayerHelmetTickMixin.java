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
package cn.nm.lms.carpetlmsaddition.mixin.util.chunk.loader.helmet;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cn.nm.lms.carpetlmsaddition.rule.Settings;
import it.unimi.dsi.fastutil.objects.ObjectSet;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerHelmetTickMixin {
    @Unique
    private static final Map<UUID, Boolean> lastRuleState = new ConcurrentHashMap<>();

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void refreshHelmetDistances$LMS(CallbackInfo _unusedCi) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        if (player.isSpectator()) {
            return;
        }

        boolean enabled = Settings.helmetControlsPlayerDistance;
        Boolean previous = lastRuleState.put(player.getUUID(), enabled);
        boolean firstSeen = previous == null;
        boolean ruleChanged = previous != null && previous != enabled;

        if (shouldRefresh(player, enabled, firstSeen, ruleChanged)) {
            refreshTickets$LMS(player);
        }
    }

    @Unique
    private void refreshTickets$LMS(ServerPlayer player) {
        ServerLevel level = player.level();
        ChunkMap chunkMap = level.getChunkSource().chunkMap;
        DistanceManager distanceManager = chunkMap.getDistanceManager();

        SectionPos sectionPos = SectionPos.of(player);
        long chunkKey = this.chunkKey$LMS(sectionPos);

        ObjectSet<ServerPlayer> trackedPlayers = distanceManager.playersPerChunk.get(chunkKey);
        if (trackedPlayers != null && trackedPlayers.contains(player)) {
            distanceManager.removePlayer(sectionPos, player);
        }

        distanceManager.addPlayer(sectionPos, player);

        chunkMap.updateChunkTracking(player);
    }

    @Unique
    private boolean shouldRefresh(ServerPlayer player, boolean enabled, boolean firstSeen, boolean ruleChanged) {
        if (!enabled) {
            return firstSeen || ruleChanged;
        }

        if (firstSeen || ruleChanged) {
            return true;
        }

        return player.tickCount % 300 == 0;
    }

    @Unique
    private long chunkKey$LMS(SectionPos sectionPos) {
        //#if MC>=260100
        return sectionPos.chunk().pack();
        //#else
        //$$ return sectionPos.chunk().toLong();
        //#endif
    }
}
