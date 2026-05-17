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

import java.util.List;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.CommandDispatcher;

import cn.nm.lms.carpetlmsaddition.bot.CommandCleanGetItemBot;
import cn.nm.lms.carpetlmsaddition.bot.CommandGetItem;
import cn.nm.lms.carpetlmsaddition.rule.util.storage.CommandGetStorageData;
import cn.nm.lms.carpetlmsaddition.rule.util.storage.CommandStorageWebsite;
import cn.nm.lms.carpetlmsaddition.safety.CommandSetPassword;

public final class SetupCommands {
    private static final List<BaseCommand> COMMANDS = List.of(new CommandLms(), new CommandStorageWebsite(),
        new CommandSetPassword(), new CommandPlayerDropall(), new CommandCleanGetItemBot());
    private static final List<BaseCommandWithContext> COMMAND_WITH_CONTEXTS =
        List.of(new CommandGetItem(), new CommandGetStorageData());

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher,
        final CommandBuildContext commandBuildContext) {
        for (BaseCommand cmd : COMMANDS) {
            cmd.register(dispatcher);
        }
        for (BaseCommandWithContext cmd : COMMAND_WITH_CONTEXTS) {
            cmd.register(dispatcher, commandBuildContext);
        }
    }
}
