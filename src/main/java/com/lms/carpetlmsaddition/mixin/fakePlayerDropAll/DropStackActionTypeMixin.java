package com.lms.carpetlmsaddition.mixin.fakePlayerDropAll;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.utils.CommandHelper;
import com.lms.carpetlmsaddition.rules.playerCommandDropall.DropAllActionExtension;
import com.lms.carpetlmsaddition.rules.playerCommandDropall.PlayerCommandDropallRuleSettings;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "carpet.helpers.EntityPlayerActionPack$ActionType$5")
public abstract class DropStackActionTypeMixin {
  @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
  private void lms$dropAllItems(
      ServerPlayer player,
      EntityPlayerActionPack.Action action,
      CallbackInfoReturnable<Boolean> cir) {
    DropAllActionExtension dropAllAction = (DropAllActionExtension) action;
    if (!dropAllAction.lms$isDropAll()) {
      return;
    }

    if (!CommandHelper.canUseCommand(
        player.createCommandSourceStack(), PlayerCommandDropallRuleSettings.playerCommandDropall)) {
      cir.setReturnValue(false);
      return;
    }

    EntityPlayerActionPack actionPack = ((ServerPlayerInterface) player).getActionPack();
    actionPack.drop(-2, true);
    cir.setReturnValue(false);
  }
}
