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
package cn.nm.lms.carpetlmsaddition.rule.util.command.lms;

import java.util.List;
import java.util.Locale;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import carpet.patches.EntityPlayerMPFake;
import carpet.utils.CommandHelper;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.util.command.BaseCommandWithContext;

public interface CommandLms extends BaseCommandWithContext {
    String ARG_VALUE = "value";
    String MESSAGE_UNKNOWN_VALUE_PREFIX = "Unknown value: ";
    String MESSAGE_VALUE_NOT_FOUND_PREFIX = "Value not found: ";
    String COMMAND = "lms";
    String ARG_PLAYER = "player";
    String MESSAGE_PREFIX = "[Carpet LMS Addition] ";
    String MESSAGE_NO_PERMISSION = "No permission";

    private static String permissionFor(CommandSourceStack src, ServerPlayer target) {
        if (target instanceof EntityPlayerMPFake) {
            return Settings.commandLMSBot;
        }
        ServerPlayer self = src.getPlayer();
        if (Utils.isSamePlayer(target, self)) {
            return Settings.commandLMSSelf;
        }
        return Settings.commandLMSOthers;
    }

    static boolean cannotUse(CommandSourceStack src, ServerPlayer target) {
        return !CommandHelper.canUseCommand(src, permissionFor(src, target));
    }

    static ServerPlayer getTarget(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return EntityArgument.getPlayer(ctx, ARG_PLAYER);
    }

    static String getValue(CommandContext<CommandSourceStack> ctx) {
        return StringArgumentType.getString(ctx, ARG_VALUE);
    }

    static void sendFailure(CommandSourceStack src, String message) {
        src.sendFailure(Component.literal(message));
    }

    static void sendNoPermission(CommandSourceStack src) {
        sendFailure(src, MESSAGE_NO_PERMISSION);
    }

    static void sendConfigValue(CommandSourceStack src, String config, String value) {
        src.sendSuccess(() -> Component.literal(MESSAGE_PREFIX + config + " = " + value), false);
    }

    @Nullable
    static <E extends Enum<E>> E parseEnum(String value, Class<E> enumClass) {
        try {
            return Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    static String enumValue(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }

    List<LmsConfigCommand> configCommands();

    @Override
    default void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        RequiredArgumentBuilder<CommandSourceStack, ?> player = Commands.argument(ARG_PLAYER, EntityArgument.player());
        for (LmsConfigCommand configCommand : configCommands()) {
            player.then(configCommand.build(commandBuildContext).requires(_src -> configCommand.enabled()));
        }
        dispatcher.register(Commands.literal(COMMAND).then(player));
    }
}
