package com.lms.carpetlmsaddition.mixin.banOpPlaceCommand;

import carpet.utils.CommandHelper;
import com.lms.carpetlmsaddition.rules.commandPlace.CommandPlaceRuleSettings;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.PlaceCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlaceCommand.class)
public abstract class PlaceCommandMixin {
  @Inject(method = "register", at = @At("TAIL"))
  private static void lms$enforcePlaceRestriction(
      CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci) {
    CommandNode<CommandSourceStack> placeNode = dispatcher.getRoot().getChild("place");
    if (placeNode == null) {
      return;
    }

    Predicate<CommandSourceStack> restricted =
        source -> CommandHelper.canUseCommand(source, CommandPlaceRuleSettings.commandPlace);

    ((PlaceRequirementMutable) placeNode).lms$setRequirement(restricted);
  }
}
