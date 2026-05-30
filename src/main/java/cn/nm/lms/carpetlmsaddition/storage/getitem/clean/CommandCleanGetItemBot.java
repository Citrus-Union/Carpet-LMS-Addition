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
package cn.nm.lms.carpetlmsaddition.storage.getitem.clean;

import java.util.List;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import cn.nm.lms.carpetlmsaddition.Mod;
import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;
import cn.nm.lms.carpetlmsaddition.lib.MessageComponent;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.util.command.BaseCommand;
import cn.nm.lms.carpetlmsaddition.rule.util.command.CommandUtils;

public final class CommandCleanGetItemBot implements BaseCommand {
    private static boolean checkPermission(CommandSourceStack source) {
        return CommandUtils.hasPermission(source, Settings.commandCleanGetItemBot, "/cleanGetItemBot",
            "commandCleanGetItemBot");
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cleanGetItemBot").executes(ctx -> tutor(ctx.getSource()))
            .then(Commands.literal("view").executes(ctx -> executeView(ctx.getSource())))
            .then(Commands.literal("clean").then(Commands.argument("max", IntegerArgumentType.integer(1))
                .executes(ctx -> executeClean(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "max"))))));
    }

    private int executeView(CommandSourceStack source) {
        if (!checkPermission(source)) {
            return 0;
        }
        AsyncTasks.run(() -> {
            int size = CleanGetItemBot.listUnspawnedGetItemBotsWithInventory().size();
            source.getServer()
                .execute(() -> new MessageComponent("cleanGetItemBot.botsWithItem", size).sendSuccess(source));
        });
        return 1;
    }

    private int tutor(CommandSourceStack source) {
        if (!checkPermission(source)) {
            return 0;
        }
        CommandUtils.tutor(source, "cleanGetItemBot", 6);
        return 1;
    }

    private int executeClean(CommandSourceStack source, int max) {
        if (!checkPermission(source)) {
            return 0;
        }
        ServerPlayer player = CommandUtils.getPlayer(source, "/cleanGetItemBot clean");
        if (player == null) {
            return 0;
        }

        new MessageComponent("cleanGetItemBot.started").sendSuccess(source);
        AsyncTasks.run(() -> {
            try {
                List<String> cleaned = CleanGetItemBot.cleanBots(player, max);
                source.getServer()
                    .execute(() -> new MessageComponent("cleanGetItemBot.cleaned", String.join(", ", cleaned))
                        .sendSuccess(source));
            } catch (Throwable throwable) {
                Mod.LOGGER.warn("cleanGetItemBot failed", throwable);
                source.getServer().execute(() -> new MessageComponent("common.unknownError").sendFailure(source));
            }
        });
        return 1;
    }
}
