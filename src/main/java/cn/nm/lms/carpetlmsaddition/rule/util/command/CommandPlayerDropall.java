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

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import carpet.helpers.EntityPlayerActionPack;
import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.mixin.util.command.playercommanddropall.PlayerCommandDropallMixin;
import cn.nm.lms.carpetlmsaddition.rule.Settings;

public final class CommandPlayerDropall implements BaseCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildDropallCommand() {
        return Commands.literal("dropall")
            .requires(src -> CommandHelper.canUseCommand(src, Settings.playerCommandDropall))
            .executes(CommandPlayerDropall::executeDropallOnce)
            .then(Commands.literal("once").executes(CommandPlayerDropall::executeDropallOnce))
            .then(Commands.literal("continuous").executes(CommandPlayerDropall::executeDropallContinuous))
            .then(Commands.literal("interval").then(Commands.argument("ticks", IntegerArgumentType.integer(1))
                .executes(CommandPlayerDropall::executeDropallInterval)));
    }

    private static void markAndStart(EntityPlayerActionPack pack, EntityPlayerActionPack.Action action) {
        ((DropallActionExtension)action).setDropall$LMS(true);
        pack.start(EntityPlayerActionPack.ActionType.DROP_STACK, action);
    }

    private static int executeDropallOnce(CommandContext<CommandSourceStack> ctx) {
        return PlayerCommandDropallMixin.invokeManipulate$LMS(ctx, pack -> pack.drop(-2, true));
    }

    private static int executeDropallContinuous(CommandContext<CommandSourceStack> ctx) {
        return PlayerCommandDropallMixin.invokeManipulate$LMS(ctx,
            pack -> markAndStart(pack, EntityPlayerActionPack.Action.continuous()));
    }

    private static int executeDropallInterval(CommandContext<CommandSourceStack> ctx) {
        int t = IntegerArgumentType.getInteger(ctx, "ticks");
        return PlayerCommandDropallMixin.invokeManipulate$LMS(ctx,
            pack -> markAndStart(pack, EntityPlayerActionPack.Action.interval(t)));
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        CommandNode<CommandSourceStack> rootRaw = dispatcher.getRoot().getChild("player");
        if (!(rootRaw instanceof LiteralCommandNode<CommandSourceStack> root)) {
            return;
        }
        CommandNode<CommandSourceStack> playerArg = root.getChild("player");
        if (playerArg == null) {
            return;
        }
        if (playerArg.getChild("dropall") == null) {
            playerArg.addChild(buildDropallCommand().build());
        }
    }

    public interface DropallActionExtension {
        boolean isDropall$LMS();

        void setDropall$LMS(boolean dropall);
    }
}
