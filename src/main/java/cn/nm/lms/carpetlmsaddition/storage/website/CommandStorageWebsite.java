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
package cn.nm.lms.carpetlmsaddition.storage.website;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.util.command.BaseCommand;

public final class CommandStorageWebsite implements BaseCommand {
    private static boolean canUseStorageWebsiteCommand(CommandSourceStack source) {
        return CommandHelper.canUseCommand(source, Settings.commandStorageWebsite);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> stop =
            Commands.literal("stop").requires(CommandStorageWebsite::canUseStorageWebsiteCommand).executes(ctx -> {
                CommandSourceStack source = ctx.getSource();
                return executeServerActionAsync(source, "stopping", Website::stopServer);
            });

        LiteralArgumentBuilder<CommandSourceStack> start =
            Commands.literal("start").requires(CommandStorageWebsite::canUseStorageWebsiteCommand).executes(ctx -> {
                CommandSourceStack source = ctx.getSource();
                return executeServerActionAsync(source, "starting", Website::startServer);
            });

        LiteralArgumentBuilder<CommandSourceStack> status =
            Commands.literal("status").requires(CommandStorageWebsite::canUseStorageWebsiteCommand).executes(ctx -> {
                CommandSourceStack source = ctx.getSource();
                return executeServerStatus(source);
            });

        dispatcher.register(Commands.literal("storageWebsite")
            .requires(CommandStorageWebsite::canUseStorageWebsiteCommand).then(start).then(stop).then(status));
    }

    private int executeServerStatus(CommandSourceStack source) {
        String statusText = Website.isServerRunning() ? "running" : "stopped";
        source.sendSuccess(() -> Component.literal("storageWebsite status: " + statusText), false);
        return 1;
    }

    private int executeServerActionAsync(CommandSourceStack source, String action, Runnable task) {
        AsyncTasks.run(() -> {
            task.run();
            source.getServer().execute(
                () -> source.sendSuccess(() -> Component.literal("storageWebsite " + action + " done"), false));
        });
        return 1;
    }
}
