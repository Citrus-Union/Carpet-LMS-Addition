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
package cn.nm.lms.carpetlmsaddition.playerconfig.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import carpet.patches.EntityPlayerMPFake;
import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.rule.Settings;

public final class PlayerConfigCommandSupport {
    public static final String ARG_VALUE = "value";
    public static final String ARG_PLAYER = "player";
    public static final String COMMAND = "lms";
    public static final String MESSAGE_PREFIX = "[Carpet LMS Addition] ";
    public static final String MESSAGE_NO_PERMISSION = "No permission";
    public static final String MESSAGE_UNKNOWN_VALUE_PREFIX = "Unknown value: ";
    public static final String MESSAGE_VALUE_NOT_FOUND_PREFIX = "Value not found: ";

    private PlayerConfigCommandSupport() {}

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

    public static boolean cannotUse(CommandSourceStack src, ServerPlayer target) {
        return !CommandHelper.canUseCommand(src, permissionFor(src, target));
    }

    public static ServerPlayer getTarget(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return EntityArgument.getPlayer(ctx, ARG_PLAYER);
    }

    public static String getValue(CommandContext<CommandSourceStack> ctx) {
        return StringArgumentType.getString(ctx, ARG_VALUE);
    }

    public static void sendFailure(CommandSourceStack src, String message) {
        src.sendFailure(Component.literal(message));
    }

    public static void sendNoPermission(CommandSourceStack src) {
        sendFailure(src, MESSAGE_NO_PERMISSION);
    }

    public static void sendConfigValue(CommandSourceStack src, String config, String value) {
        src.sendSuccess(() -> Component.literal(MESSAGE_PREFIX + config + " = " + value), false);
    }
}
