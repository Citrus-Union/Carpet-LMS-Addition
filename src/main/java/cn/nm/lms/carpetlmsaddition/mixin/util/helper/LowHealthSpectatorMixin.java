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
package cn.nm.lms.carpetlmsaddition.mixin.util.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.nm.lms.carpetlmsaddition.Mod;
import cn.nm.lms.carpetlmsaddition.lib.PlayerConfig;
import cn.nm.lms.carpetlmsaddition.lib.check.CheckMod;
import cn.nm.lms.carpetlmsaddition.rule.Settings;

@Mixin(ServerPlayer.class)
public abstract class LowHealthSpectatorMixin {
    @Unique
    private static final Map<UUID, Long> COOLDOWN_MAP = new HashMap<>();

    @Unique
    private static void spectator(ServerPlayer player) {
        switch (Settings.lowHealthSpectatorMethod) {
            case "vanilla" -> player.setGameMode(GameType.SPECTATOR);
            case "mcdreforged" -> Mod.LOGGER.info("<{}> !!spec", player.getName().getString());
            case "carpet-org-addition" -> {
                if (CheckMod.checkMod("carpet-org-addition", ">=1.38.0")) {
                    MinecraftServer server = player.level().getServer();
                    Commands commandManager = server.getCommands();
                    server.execute(
                        () -> commandManager.performPrefixedCommand(player.createCommandSourceStack(), "spectator"));
                } else {
                    Mod.LOGGER.warn("Carpet Org Addition (>=1.38.0) not installed");
                }
            }
            case "kick" -> player.connection.disconnect(Component.nullToEmpty("Kicked by Carpet LMS Addition"));
            default -> Mod.LOGGER.warn("Unknown lowHealthSpectatorMethod: {}", Settings.lowHealthSpectatorMethod);
        }

    }

    @Unique
    private static boolean isInCooldown(long now, long last) {
        return now - last < Settings.lowHealthSpectatorCooldown;
    }

    @Unique
    private static boolean isEnabled(UUID playerUUID) {
        String value = Settings.lowHealthSpectator;
        if ("true".equals(value)) {
            return true;
        }
        if ("false".equals(value)) {
            return false;
        }

        if ("custom".equals(value)) {
            String configured = PlayerConfig.get(playerUUID, "lowHealthSpectator");
            return Boolean.parseBoolean(configured);
        }
        return false;
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void spectatorAfterHurt$lms(CallbackInfoReturnable<Boolean> cir) {

        if (!cir.getReturnValue()) {
            return;
        }

        ServerPlayer player = (ServerPlayer)(Object)this;

        long now = player.level().getGameTime();
        UUID playerUUID = player.getUUID();
        long last = COOLDOWN_MAP.getOrDefault(playerUUID, now - Settings.lowHealthSpectatorCooldown);

        if (!isEnabled(playerUUID)) {
            return;
        }
        if (isInCooldown(now, last)) {
            return;
        }
        if (player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) {
            return;
        }

        float hp = player.getHealth();
        if (hp <= 0f) {
            return;
        }
        if (hp > Settings.lowHealthSpectatorThreshold) {
            return;
        }

        spectator(player);

        COOLDOWN_MAP.put(playerUUID, now);
    }
}
