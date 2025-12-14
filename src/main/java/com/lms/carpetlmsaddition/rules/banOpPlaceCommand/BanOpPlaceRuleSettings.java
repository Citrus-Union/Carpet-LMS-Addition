package com.lms.carpetlmsaddition.rules.banOpPlaceCommand;

import static carpet.api.settings.RuleCategory.SURVIVAL;

import carpet.CarpetServer;
import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.Validator;
import carpet.utils.CommandHelper;
import com.lms.carpetlmsaddition.lib.CarpetLmsTranslations;
import com.lms.carpetlmsaddition.lib.RuleSupport;
import net.minecraft.commands.CommandSourceStack;

public final class BanOpPlaceRuleSettings {
  private BanOpPlaceRuleSettings() {}

  @Rule(
      categories = {RuleSupport.LMS, SURVIVAL},
      validators = CommandsRefreshValidator.class)
  public static boolean banOpPlaceCommand = false;

  public static final class CommandsRefreshValidator extends Validator<Boolean> {
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
      return CarpetLmsTranslations.translate("carpet.validator.commandsRefresh");
    }
  }
}
