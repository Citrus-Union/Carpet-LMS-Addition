package com.lms.carpetlmsaddition.mixin.fakePlayerDropAll;

import carpet.CarpetServer;
import carpet.commands.PlayerCommand;
import carpet.helpers.EntityPlayerActionPack;
import carpet.utils.CommandHelper;
import com.lms.carpetlmsaddition.rules.playerCommandDropall.DropAllActionExtension;
import com.lms.carpetlmsaddition.rules.playerCommandDropall.PlayerCommandDropallRuleSettings;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerCommand.class)
public abstract class PlayerCommandMixin {
  @Inject(method = "register", at = @At("TAIL"))
  private static void lms$registerDropAll(
      com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher,
      net.minecraft.commands.CommandBuildContext ctx,
      CallbackInfo ci) {
    CommandNode<CommandSourceStack> playerRootRaw = dispatcher.getRoot().getChild("player");
    if (!(playerRootRaw instanceof LiteralCommandNode<CommandSourceStack> playerRoot)) {
      return;
    }
    CommandNode<CommandSourceStack> playerArg = playerRoot.getChild("player");
    if (playerArg == null) {
      return;
    }

    if (playerArg.getChild("dropall") == null) {
      playerArg.addChild(lms$dropAllCommand().build());
      CommandHelper.notifyPlayersCommandsChanged(CarpetServer.minecraft_server);
    }
  }

  @Invoker(value = "manipulation", remap = false)
  private static Command<CommandSourceStack> lms$manipulation(
      Consumer<EntityPlayerActionPack> manipulation) {
    throw new AssertionError();
  }

  @Invoker(value = "manipulate", remap = false)
  private static int lms$manipulate(
      CommandContext<CommandSourceStack> context, Consumer<EntityPlayerActionPack> manipulation) {
    throw new AssertionError();
  }

  @Invoker(value = "getPlayerSuggestions", remap = false)
  private static Collection<String> lms$getPlayerSuggestions(CommandSourceStack source) {
    throw new AssertionError();
  }

  @Unique
  private static LiteralArgumentBuilder<CommandSourceStack> lms$dropAllCommand() {
    return Commands.literal("dropall")
        .requires(
            source ->
                CommandHelper.canUseCommand(
                    source, PlayerCommandDropallRuleSettings.playerCommandDropall))
        .executes(PlayerCommandMixin::lms$dropAllOnce)
        .then(Commands.literal("once").executes(PlayerCommandMixin::lms$dropAllOnce))
        .then(Commands.literal("continuous").executes(PlayerCommandMixin::lms$dropAllContinuous))
        .then(
            Commands.literal("interval")
                .then(
                    Commands.argument("ticks", IntegerArgumentType.integer(1))
                        .executes(PlayerCommandMixin::lms$dropAllInterval)));
  }

  @Unique
  private static void lms$startDropAll(
      EntityPlayerActionPack actionPack, EntityPlayerActionPack.Action action) {
    ((DropAllActionExtension) (Object) action).lms$setDropAll(true);
    actionPack.start(EntityPlayerActionPack.ActionType.DROP_STACK, action);
  }

  @Unique
  private static void lms$dropAllNow(EntityPlayerActionPack actionPack) {
    actionPack.drop(-2, true);
  }

  @Unique
  private static int lms$dropAllOnce(CommandContext<CommandSourceStack> context) {
    return lms$manipulate(context, PlayerCommandMixin::lms$dropAllNow);
  }

  @Unique
  private static int lms$dropAllContinuous(CommandContext<CommandSourceStack> context) {
    return lms$manipulate(
        context, pack -> lms$startDropAll(pack, EntityPlayerActionPack.Action.continuous()));
  }

  @Unique
  private static int lms$dropAllInterval(CommandContext<CommandSourceStack> context) {
    int interval = IntegerArgumentType.getInteger(context, "ticks");
    return lms$manipulate(
        context, pack -> lms$startDropAll(pack, EntityPlayerActionPack.Action.interval(interval)));
  }
}
