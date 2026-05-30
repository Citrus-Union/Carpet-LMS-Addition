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

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import cn.nm.lms.carpetlmsaddition.playerconfig.PlayerConfigs;
import cn.nm.lms.carpetlmsaddition.rule.util.command.BaseCommandWithContext;
import cn.nm.lms.carpetlmsaddition.rule.util.command.CommandUtils;

public final class CommandLms implements BaseCommandWithContext {

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        RequiredArgumentBuilder<CommandSourceStack, ?> player =
            Commands.argument(PlayerConfigCommandSupport.ARG_PLAYER, EntityArgument.player());
        for (var configCommand : PlayerConfigs.ALL) {
            player.then(configCommand.build(commandBuildContext).requires(_src -> configCommand.enabled()));
        }
        dispatcher.register(
            Commands.literal(PlayerConfigCommandSupport.COMMAND).executes(ctx -> tutor(ctx.getSource())).then(player));
    }

    private int tutor(CommandSourceStack source) {
        CommandUtils.tutor(source, "playerConfig", 5);
        return 1;
    }
}
