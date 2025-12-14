package com.lms.carpetlmsaddition.rules.commandPlace;

import static carpet.api.settings.RuleCategory.SURVIVAL;

import carpet.CarpetServer;
import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.Validator;
import carpet.utils.CommandHelper;
import com.lms.carpetlmsaddition.lib.CarpetLmsTranslations;
import com.lms.carpetlmsaddition.lib.RuleSupport;
import net.minecraft.commands.CommandSourceStack;

public final class CommandPlaceRuleSettings {
  private CommandPlaceRuleSettings() {}

  @Rule(
      categories = {RuleSupport.LMS, SURVIVAL},
      options = {"true", "false", "ops", "0", "1", "2", "3", "4"},
      validators = CommandsRefreshValidator.class)
  public static String commandPlace = "ops";

  public static final class CommandsRefreshValidator extends Validator<String> {
    @Override
    public String validate(
        CommandSourceStack source, CarpetRule<String> rule, String newValue, String userInput) {
      if (CarpetServer.minecraft_server != null) {
        CommandHelper.notifyPlayersCommandsChanged(CarpetServer.minecraft_server);
      }
      return newValue;
    }

    @Override
    public String description() {
      return CarpetLmsTranslations.translate("carpet.validator.commandsRefresh");
    }
  }
}
