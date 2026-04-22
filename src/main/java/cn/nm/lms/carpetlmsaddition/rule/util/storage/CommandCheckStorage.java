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
package cn.nm.lms.carpetlmsaddition.rule.util.storage;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.util.command.BaseCommand;
import cn.nm.lms.carpetlmsaddition.rule.util.storage.website.Website;

public final class CommandCheckStorage implements BaseCommand {
    private static boolean canUseCheckStorageDataCommand(CommandSourceStack source) {
        return CommandHelper.canUseCommand(source, Settings.commandCheckStorageData);
    }

    private static boolean canUseCheckStorageServerCommand(CommandSourceStack source) {
        return CommandHelper.canUseCommand(source, Settings.commandCheckStorageServer);
    }

    private static boolean canUseAnyCheckStorageCommand(CommandSourceStack source) {
        return canUseCheckStorageDataCommand(source) || canUseCheckStorageServerCommand(source);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> stopServer = Commands.literal("stopServer")
            .requires(CommandCheckStorage::canUseCheckStorageServerCommand).executes(ctx -> {
                CommandSourceStack source = ctx.getSource();
                return executeServerActionAsync(source, "stopping", Website::stopServer);
            });

        LiteralArgumentBuilder<CommandSourceStack> startServer = Commands.literal("startServer")
            .requires(CommandCheckStorage::canUseCheckStorageServerCommand).executes(ctx -> {
                CommandSourceStack source = ctx.getSource();
                return executeServerActionAsync(source, "starting", Website::startServer);
            });

        LiteralArgumentBuilder<CommandSourceStack> serverStatus = Commands.literal("serverStatus")
            .requires(CommandCheckStorage::canUseCheckStorageServerCommand).executes(ctx -> {
                CommandSourceStack source = ctx.getSource();
                return executeServerStatus(source);
            });

        LiteralArgumentBuilder<CommandSourceStack> updateData = Commands.literal("updateData")
            .requires(CommandCheckStorage::canUseCheckStorageDataCommand).executes(ctx -> {
                CommandSourceStack source = ctx.getSource();
                return executeUpdateData(source);
            });

        dispatcher.register(Commands.literal("checkStorage").requires(CommandCheckStorage::canUseAnyCheckStorageCommand)
            .then(updateData).then(startServer).then(stopServer).then(serverStatus));
    }

    private int executeUpdateData(CommandSourceStack source) {
        String text = Storage.checkStorage();
        source.sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private int executeServerStatus(CommandSourceStack source) {
        String statusText = Website.isServerRunning() ? "running" : "stopped";
        source.sendSuccess(() -> Component.literal("checkStorage server status: " + statusText), false);
        return 1;
    }

    private int executeServerActionAsync(CommandSourceStack source, String action, Runnable task) {
        source.sendSuccess(() -> Component.literal("checkStorage server " + action + " in background"), false);
        AsyncTasks.run(() -> {
            task.run();
            source.getServer().execute(
                () -> source.sendSuccess(() -> Component.literal("checkStorage server " + action + " done"), false));
        });
        return 1;
    }
}
