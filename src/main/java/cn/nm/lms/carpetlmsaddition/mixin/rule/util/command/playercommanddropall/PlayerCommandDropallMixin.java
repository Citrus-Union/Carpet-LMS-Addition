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
package cn.nm.lms.carpetlmsaddition.mixin.rule.util.command.playercommanddropall;

import java.util.function.Consumer;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import carpet.commands.PlayerCommand;
import carpet.helpers.EntityPlayerActionPack;
import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.rule.util.command.playercommanddropall.DropallActionExtension;
import cn.nm.lms.carpetlmsaddition.rule.util.command.playercommanddropall.PlayerCommandDropallRule;

@Mixin(
        value = PlayerCommand.class,
        remap = false
)
public abstract class PlayerCommandDropallMixin
{
    @Inject(
            method = "register",
            at = @At(
                "TAIL"
            )
    )
    private static void registerDropall$LMS(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext ctx,
            CallbackInfo ci
    )
    {
        CommandNode<CommandSourceStack> rootRaw = dispatcher.getRoot().getChild("player");
        if (!(rootRaw instanceof LiteralCommandNode<CommandSourceStack> root)) return;
        CommandNode<CommandSourceStack> playerArg = root.getChild("player");
        if (playerArg == null)
        {
            return;
        }
        if (playerArg.getChild("dropall") == null)
        {
            playerArg.addChild(buildDropallCommand$LMS().build());
        }
    }

    @Invoker(
            value = "manipulate",
            remap = false
    )
    private static int invokeManipulate$LMS(
            CommandContext<CommandSourceStack> context,
            Consumer<EntityPlayerActionPack> op
    )
    {
        throw new AssertionError();
    }

    @Unique
    private static LiteralArgumentBuilder<CommandSourceStack> buildDropallCommand$LMS()
    {
        return Commands.literal("dropall")
                       .requires(
                               src -> CommandHelper.canUseCommand(
                                       src,
                                       PlayerCommandDropallRule.playerCommandDropall
                               )
                       )
                       .executes(PlayerCommandDropallMixin::executeDropallOnce$LMS)
                       .then(
                               Commands.literal("once")
                                       .executes(PlayerCommandDropallMixin::executeDropallOnce$LMS)
                       )
                       .then(
                               Commands.literal("continuous")
                                       .executes(
                                               PlayerCommandDropallMixin::executeDropallContinuous$LMS
                                       )
                       )
                       .then(
                               Commands.literal("interval")
                                       .then(
                                               Commands.argument(
                                                       "ticks",
                                                       IntegerArgumentType.integer(1)
                                               )
                                                       .executes(
                                                               PlayerCommandDropallMixin::executeDropallInterval$LMS
                                                       )
                                       )
                       );
    }

    @Unique
    private static void markAndStart$LMS(
            EntityPlayerActionPack pack,
            EntityPlayerActionPack.Action action
    )
    {
        ((DropallActionExtension) action).setDropall$LMS(true);
        pack.start(EntityPlayerActionPack.ActionType.DROP_STACK, action);
    }

    @Unique
    private static int executeDropallOnce$LMS(CommandContext<CommandSourceStack> ctx)
    {
        return invokeManipulate$LMS(ctx, pack -> pack.drop(-2, true));
    }

    @Unique
    private static int executeDropallContinuous$LMS(CommandContext<CommandSourceStack> ctx)
    {
        return invokeManipulate$LMS(
                ctx,
                pack -> markAndStart$LMS(pack, EntityPlayerActionPack.Action.continuous())
        );
    }

    @Unique
    private static int executeDropallInterval$LMS(CommandContext<CommandSourceStack> ctx)
    {
        int t = IntegerArgumentType.getInteger(ctx, "ticks");
        return invokeManipulate$LMS(
                ctx,
                pack -> markAndStart$LMS(pack, EntityPlayerActionPack.Action.interval(t))
        );
    }
}
