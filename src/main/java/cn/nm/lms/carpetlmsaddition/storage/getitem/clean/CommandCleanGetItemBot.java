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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.util.command.BaseCommand;

public final class CommandCleanGetItemBot implements BaseCommand {
    private static boolean canUse(CommandSourceStack source) {
        return CommandHelper.canUseCommand(source, Settings.commandCleanGetItemBot);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cleanGetItemBot").requires(CommandCleanGetItemBot::canUse)
            .then(Commands.literal("view").executes(ctx -> executeView(ctx.getSource())))
            .then(Commands.literal("clean").then(Commands.argument("max", IntegerArgumentType.integer(1))
                .executes(ctx -> executeClean(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "max"))))));
    }

    private int executeView(CommandSourceStack source) {
        AsyncTasks.run(() -> {
            int size = CleanGetItemBot.listUnspawnedGetItemBotsWithInventory().size();
            source.getServer()
                .execute(() -> source.sendSuccess(() -> Component.literal(size + " bots with item"), false));
        });
        return 1;
    }

    private int executeClean(CommandSourceStack source, int max) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("cleanGetItemBot clean can only be used by a player"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("cleanGetItemBot clean running in background"), false);
        AsyncTasks.run(() -> {
            try {
                List<String> cleaned = CleanGetItemBot.cleanBots(player, max);
                source.getServer().execute(() -> source.sendSuccess(
                    () -> Component.literal("cleanGetItemBot cleaned: " + String.join(", ", cleaned)), false));
            } catch (Throwable throwable) {
                String msg = throwable.getMessage();
                source.getServer().execute(() -> source.sendFailure(
                    Component.literal("cleanGetItemBot clean failed: " + (msg == null ? "unknown" : msg))));
            }
        });
        return 1;
    }
}
