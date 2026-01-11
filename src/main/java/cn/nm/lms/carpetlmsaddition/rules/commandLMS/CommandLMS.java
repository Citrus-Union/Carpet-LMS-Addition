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
package cn.nm.lms.carpetlmsaddition.rules.commandLMS;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.lib.PlayerConfig;

public final class CommandLMS
{
    public static final Map<String, Set<String>> ALL_CONFIG = Map.of(
            "lowHealthSpectator",
            Set.of("true", "false")
    );

    private CommandLMS()
    {
    }

    private static Set<String> configList()
    {
        return ALL_CONFIG.keySet();
    }

    private static Set<String> valuesOf(String config)
    {
        return ALL_CONFIG.getOrDefault(config, Collections.emptySet());
    }

    private static boolean hasConfig(String config)
    {
        return ALL_CONFIG.containsKey(config);
    }

    private static boolean hasValue(String config, String value)
    {
        return hasConfig(config) && valuesOf(config).contains(value);
    }

    private static boolean canUse(CommandSourceStack src, ServerPlayer target)
    {
        ServerPlayer self = src.getPlayer();
        boolean isSelf = self != null && self.getUUID().equals(target.getUUID());
        String perm = isSelf ? CommandLMSSelf.commandLMSSelf : CommandLMSOthers.commandLMSOthers;
        return CommandHelper.canUseCommand(src, perm);
    }

    public static void register()
    {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> dispatcher.register(
                        Commands.literal("lms")
                                .then(
                                        Commands.argument("player", EntityArgument.player())
                                                .then(
                                                        Commands.argument(
                                                                "config",
                                                                StringArgumentType.word()
                                                        ).suggests((context, builder) ->
                                                        {
                                                            configList().forEach(builder::suggest);
                                                            return builder.buildFuture();
                                                        }).executes(ctx ->
                                                        {
                                                            CommandSourceStack src = ctx.getSource();
                                                            ServerPlayer target = EntityArgument.getPlayer(
                                                                    ctx,
                                                                    "player"
                                                            );
                                                            String config = StringArgumentType.getString(
                                                                    ctx,
                                                                    "config"
                                                            );
                                                            if (!hasConfig(config))
                                                            {
                                                                src.sendFailure(
                                                                        Component.literal(
                                                                                "Unknown config: " + config
                                                                        )
                                                                );
                                                                return 0;
                                                            }
                                                            if (!canUse(src, target))
                                                            {
                                                                src.sendFailure(
                                                                        Component.literal(
                                                                                "No permission"
                                                                        )
                                                                );
                                                                return 0;
                                                            }
                                                            String raw = PlayerConfig.get(
                                                                    target.getUUID(),
                                                                    config
                                                            );
                                                            src.sendSuccess(
                                                                    () -> Component.literal(
                                                                            "[Carpet LMS " + "Addition] " + config + " = " + (raw == null ? "null" : raw)
                                                                    ),
                                                                    false
                                                            );
                                                            return 1;
                                                        })
                                                                .then(
                                                                        Commands.argument(
                                                                                "value",
                                                                                StringArgumentType.word()
                                                                        ).suggests((ctx, builder) ->
                                                                        {
                                                                            String config = StringArgumentType.getString(
                                                                                    ctx,
                                                                                    "config"
                                                                            );
                                                                            valuesOf(config)
                                                                                            .forEach(
                                                                                                    builder::suggest
                                                                                            );
                                                                            return builder.buildFuture();
                                                                        }).executes(ctx ->
                                                                        {
                                                                            CommandSourceStack src = ctx.getSource();
                                                                            ServerPlayer target = EntityArgument.getPlayer(
                                                                                    ctx,
                                                                                    "player"
                                                                            );
                                                                            String config = StringArgumentType.getString(
                                                                                    ctx,
                                                                                    "config"
                                                                            );
                                                                            String value = StringArgumentType.getString(
                                                                                    ctx,
                                                                                    "value"
                                                                            );
                                                                            if (!hasValue(
                                                                                    config,
                                                                                    value
                                                                            ))
                                                                            {
                                                                                src.sendFailure(
                                                                                        Component.literal(
                                                                                                "Unknown config or value: " + config + " " + value
                                                                                        )
                                                                                );
                                                                                return 0;
                                                                            }
                                                                            if (!canUse(
                                                                                    src,
                                                                                    target
                                                                            ))
                                                                            {
                                                                                src.sendFailure(
                                                                                        Component.literal(
                                                                                                "No permission"
                                                                                        )
                                                                                );
                                                                                return 0;
                                                                            }
                                                                            PlayerConfig.set(
                                                                                    target.getUUID(),
                                                                                    config,
                                                                                    value
                                                                            );
                                                                            src.sendSuccess(
                                                                                    () -> Component.literal(
                                                                                            "[Carpet LMS Addition] " + config + " = " + value
                                                                                    ),
                                                                                    false
                                                                            );
                                                                            return 1;
                                                                        })
                                                                )
                                                )
                                )
                )
        );
    }
}
