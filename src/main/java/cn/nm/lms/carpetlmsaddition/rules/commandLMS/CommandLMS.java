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
import java.util.concurrent.CompletableFuture;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.lib.PlayerConfig;

public final class CommandLMS
{
    private static final String COMMAND = "lms";
    private static final String ARG_PLAYER = "player";
    private static final String ARG_CONFIG = "config";
    private static final String ARG_VALUE = "value";
    private static final String MESSAGE_PREFIX = "[Carpet LMS Addition] ";
    private static final String MESSAGE_NO_PERMISSION = "No permission";
    private static final String MESSAGE_UNKNOWN_CONFIG_PREFIX = "Unknown config: ";
    private static final String MESSAGE_UNKNOWN_CONFIG_OR_VALUE_PREFIX = "Unknown config or value: ";

    public static final Map<String, Set<String>> ALL_CONFIG = Map.of(
            "lowHealthSpectator",
            Set.of("true", "false")
    );

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

    private static boolean cannotUse(CommandSourceStack src, ServerPlayer target)
    {
        ServerPlayer self = src.getPlayer();
        boolean isSelf = self != null && self.getUUID().equals(target.getUUID());
        String perm = isSelf ? CommandLMSSelf.commandLMSSelf : CommandLMSOthers.commandLMSOthers;
        return !CommandHelper.canUseCommand(src, perm);
    }

    private static ServerPlayer getTarget(CommandContext<CommandSourceStack> ctx)
                                                                                  throws CommandSyntaxException
    {
        return EntityArgument.getPlayer(ctx, ARG_PLAYER);
    }

    private static String getConfig(CommandContext<CommandSourceStack> ctx)
    {
        return StringArgumentType.getString(ctx, ARG_CONFIG);
    }

    private static String getValue(CommandContext<CommandSourceStack> ctx)
    {
        return StringArgumentType.getString(ctx, ARG_VALUE);
    }

    private static void sendFailure(CommandSourceStack src, String message)
    {
        src.sendFailure(Component.literal(message));
    }

    private static void sendNoPermission(CommandSourceStack src)
    {
        sendFailure(src, MESSAGE_NO_PERMISSION);
    }

    private static void sendConfigValue(CommandSourceStack src, String config, String value)
    {
        src.sendSuccess(() -> Component.literal(MESSAGE_PREFIX + config + " = " + value), false);
    }

    private static CompletableFuture<Suggestions> suggestConfigs(
            CommandContext<CommandSourceStack> _context,
            SuggestionsBuilder builder
    )
    {
        configList().forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestValues(
            CommandContext<CommandSourceStack> ctx,
            SuggestionsBuilder builder
    )
    {
        valuesOf(getConfig(ctx)).forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static int executeGet(CommandContext<CommandSourceStack> ctx)
                                                                          throws CommandSyntaxException
    {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer target = getTarget(ctx);
        String config = getConfig(ctx);
        if (!hasConfig(config))
        {
            sendFailure(src, MESSAGE_UNKNOWN_CONFIG_PREFIX + config);
            return 0;
        }
        if (cannotUse(src, target))
        {
            sendNoPermission(src);
            return 0;
        }

        String raw = PlayerConfig.get(target.getUUID(), config);
        sendConfigValue(src, config, raw == null ? "null" : raw);
        return 1;
    }

    private static int executeSet(CommandContext<CommandSourceStack> ctx)
                                                                          throws CommandSyntaxException
    {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer target = getTarget(ctx);
        String config = getConfig(ctx);
        String value = getValue(ctx);
        if (!hasValue(config, value))
        {
            sendFailure(src, MESSAGE_UNKNOWN_CONFIG_OR_VALUE_PREFIX + config + " " + value);
            return 0;
        }
        if (cannotUse(src, target))
        {
            sendNoPermission(src);
            return 0;
        }

        PlayerConfig.set(target.getUUID(), config, value);
        sendConfigValue(src, config, value);
        return 1;
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> buildValueArg()
    {
        return Commands.argument(ARG_VALUE, StringArgumentType.word())
                       .suggests(CommandLMS::suggestValues)
                       .executes(CommandLMS::executeSet);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> buildConfigArg()
    {
        return Commands.argument(ARG_CONFIG, StringArgumentType.word())
                       .suggests(CommandLMS::suggestConfigs)
                       .executes(CommandLMS::executeGet)
                       .then(buildValueArg());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> buildPlayerArg()
    {
        return Commands.argument(ARG_PLAYER, EntityArgument.player()).then(buildConfigArg());
    }

    public static void register()
    {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> dispatcher.register(
                        Commands.literal(COMMAND).then(buildPlayerArg())
                )
        );
    }
}
