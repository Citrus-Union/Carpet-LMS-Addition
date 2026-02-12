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
package cn.nm.lms.carpetlmsaddition.rule.util.helper.lowhealthspectator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

import cn.nm.lms.carpetlmsaddition.CarpetLMSAdditionMod;
import cn.nm.lms.carpetlmsaddition.lib.PlayerConfig;
import cn.nm.lms.carpetlmsaddition.lib.check.CheckMod;
import cn.nm.lms.carpetlmsaddition.rule.util.command.commandlms.CommandLMSRule;

public final class LowHealthSpectator
{
    private static final Map<UUID, Long> COOLDOWN_MAP = new HashMap<>();

    public static void init()
    {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (
                        entity,
                        ignoreSource,
                        ignoreBaseDamageTaken,
                        ignoreDamageTaken,
                        ignoreBlockedDamage
                ) ->
                {
                    if (!(entity instanceof ServerPlayer player))
                    {
                        return;
                    }
                    long now = player.level().getGameTime();
                    UUID playerUUID = player.getUUID();
                    long last = COOLDOWN_MAP.getOrDefault(
                            playerUUID,
                            now - LowHealthSpectatorRule.lowHealthSpectatorCooldown
                    );
                    float hp = player.getHealth();
                    if (isEnabled(
                            playerUUID
                    ) && now - last >= LowHealthSpectatorRule.lowHealthSpectatorCooldown && player.gameMode() == GameType.SURVIVAL && hp > 0f && hp <= (float) CommandLMSRule.lowHealthSpectatorThreshold)
                    {
                        switch (LowHealthSpectatorRule.lowHealthSpectatorMethod)
                        {
                            case "vanilla" -> player.setGameMode(GameType.SPECTATOR);
                            case "mcdreforged" -> CarpetLMSAdditionMod.LOGGER.info(
                                    "<{}> !!spec",
                                    player.getName().getString()
                            );
                            case "carpet-org-addition" -> {
                                if (CheckMod.checkMod("carpet-org-addition", ">=1.38.0"))
                                {
                                    MinecraftServer server = player.level().getServer();
                                    Commands commandManager = server.getCommands();
                                    server.execute(
                                            () -> commandManager.performPrefixedCommand(
                                                    player.createCommandSourceStack(),
                                                    "spectator"
                                            )
                                    );
                                }
                                else
                                {
                                    CarpetLMSAdditionMod.LOGGER.warn(
                                            "Carpet Org Addition (>=1.38.0) not installed"
                                    );
                                }
                            }
                            case "kick" -> player.connection.disconnect(
                                    Component.nullToEmpty("Kicked by Carpet LMS Addition")
                            );
                            default -> CarpetLMSAdditionMod.LOGGER.warn(
                                    "Unknown lowHealthSpectatorMethod: {}",
                                    LowHealthSpectatorRule.lowHealthSpectatorMethod
                            );
                        }
                        COOLDOWN_MAP.put(playerUUID, now);
                    }
                }
        );
    }

    private static boolean isEnabled(UUID playerUUID)
    {
        String value = LowHealthSpectatorRule.lowHealthSpectator;
        if ("true".equals(value))
        {
            return true;
        }
        if ("false".equals(value))
        {
            return false;
        }
        if ("custom".equals(value))
        {
            String configured = PlayerConfig.get(playerUUID, "lowHealthSpectator");
            return Boolean.parseBoolean(configured);
        }
        return false;
    }
}
