package com.lms.carpetlmsaddition;

import carpet.CarpetServer;
import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import carpet.utils.CommandHelper;
import net.minecraft.commands.CommandSourceStack;

public class DropAllRuleValidator extends Validator<Boolean> {
  @Override
  public Boolean validate(
      CommandSourceStack source, CarpetRule<Boolean> rule, Boolean newValue, String userInput) {
    if (CarpetServer.minecraft_server != null) {
      CommandHelper.notifyPlayersCommandsChanged(CarpetServer.minecraft_server);
    }
    return newValue;
  }

  @Override
  public String description() {
    return "Refreshes commands when fakePlayerDropAll changes.";
  }
}
