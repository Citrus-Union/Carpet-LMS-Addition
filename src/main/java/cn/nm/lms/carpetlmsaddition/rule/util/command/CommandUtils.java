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
package cn.nm.lms.carpetlmsaddition.rule.util.command;

import java.util.stream.IntStream;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.lib.MessageComponent;

public final class CommandUtils {
    public static void tutor(CommandSourceStack source, String baseName, int count) {
        IntStream.range(0, count).forEach(index -> {
            new MessageComponent(baseName + ".tutor." + index).sendSuccess(source);
        });
    }

    public static ServerPlayer getPlayer(CommandSourceStack source, String command) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            new MessageComponent("common.command.playerOnly", command).sendFailure(source);
        }
        return player;
    }

    public static boolean hasPermission(CommandSourceStack source, String permission, String command, String ruleName) {
        if (CommandHelper.canUseCommand(source, permission)) {
            return true;
        }
        new MessageComponent("common.command.noPermission", command, ruleName).sendFailure(source);
        return false;
    }
}
