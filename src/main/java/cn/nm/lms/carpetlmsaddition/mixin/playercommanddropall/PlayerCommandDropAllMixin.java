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
package cn.nm.lms.carpetlmsaddition.mixin.playercommanddropall;

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

import cn.nm.lms.carpetlmsaddition.rules.playercommanddropall.DropAllActionExtension;
import cn.nm.lms.carpetlmsaddition.rules.playercommanddropall.PlayerCommandDropall;

@Mixin(
        value = PlayerCommand.class,
        remap = false
)
public abstract class PlayerCommandDropAllMixin
{
    @Inject(
            method = "register",
            at = @At(
                "TAIL"
            )
    )
    private static void lms$registerDropAll(
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
            playerArg.addChild(cmd().build());
        }
    }

    @Invoker(
            value = "manipulate",
            remap = false
    )
    private static int manipulate(
            CommandContext<CommandSourceStack> context,
            Consumer<EntityPlayerActionPack> op
    )
    {
        throw new AssertionError();
    }

    @Unique
    private static LiteralArgumentBuilder<CommandSourceStack> cmd()
    {
        return Commands.literal("dropall")
                       .requires(
                               src -> CommandHelper.canUseCommand(
                                       src,
                                       PlayerCommandDropall.playerCommandDropall
                               )
                       )
                       .executes(PlayerCommandDropAllMixin::once)
                       .then(Commands.literal("once").executes(PlayerCommandDropAllMixin::once))
                       .then(
                               Commands.literal("continuous")
                                       .executes(PlayerCommandDropAllMixin::continuous)
                       )
                       .then(
                               Commands.literal("interval")
                                       .then(
                                               Commands.argument(
                                                       "ticks",
                                                       IntegerArgumentType.integer(1)
                                               ).executes(PlayerCommandDropAllMixin::interval)
                                       )
                       );
    }

    @Unique
    private static void markAndStart(
            EntityPlayerActionPack pack,
            EntityPlayerActionPack.Action action
    )
    {
        ((DropAllActionExtension) action).lms$setDropAll(true);
        pack.start(EntityPlayerActionPack.ActionType.DROP_STACK, action);
    }

    @Unique
    private static int once(CommandContext<CommandSourceStack> ctx)
    {
        return manipulate(ctx, pack -> pack.drop(-2, true));
    }

    @Unique
    private static int continuous(CommandContext<CommandSourceStack> ctx)
    {
        return manipulate(
                ctx,
                pack -> markAndStart(pack, EntityPlayerActionPack.Action.continuous())
        );
    }

    @Unique
    private static int interval(CommandContext<CommandSourceStack> ctx)
    {
        int t = IntegerArgumentType.getInteger(ctx, "ticks");
        return manipulate(
                ctx,
                pack -> markAndStart(pack, EntityPlayerActionPack.Action.interval(t))
        );
    }
}
