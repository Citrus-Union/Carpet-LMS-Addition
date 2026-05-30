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
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import carpet.patches.EntityPlayerMPFake;
import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.lib.MessageComponent;
import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.rule.Settings;

public final class PlayerConfigCommandSupport {
    public static final String ARG_VALUE = "value";
    public static final String ARG_PLAYER = "player";
    public static final String COMMAND = "lms";

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

    public static void sendNoPermission(CommandSourceStack src) {
        new MessageComponent("playerConfig.noPermission").sendFailure(src);
    }

    public static void sendUnknownValue(CommandSourceStack src, String value) {
        new MessageComponent("playerConfig.unknownValue", value).sendFailure(src);
    }

    public static void sendValueNotFound(CommandSourceStack src, String value) {
        new MessageComponent("playerConfig.valueNotFound", value).sendFailure(src);
    }

    public static void sendUnknownBlockNotItem(CommandSourceStack src) {
        new MessageComponent("playerConfig.unknownBlockNotItem").sendFailure(src);
    }

    public static void sendConfigValue(CommandSourceStack src, String config, String value) {
        new MessageComponent("playerConfig.value", config, value).sendSuccess(src);
    }
}
