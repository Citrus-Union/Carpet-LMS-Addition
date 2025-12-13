package com.lms.carpetlmsaddition.rules.allayHealInterval;

import static carpet.api.settings.RuleCategory.SURVIVAL;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.Validator;
import com.lms.carpetlmsaddition.lib.CarpetLmsTranslations;
import com.lms.carpetlmsaddition.lib.RuleSupport;
import net.minecraft.commands.CommandSourceStack;

public final class AllayHealRuleSettings {
  private AllayHealRuleSettings() {}

  @Rule(
      categories = {RuleSupport.LMS, SURVIVAL},
      validators = PositiveIntValidator.class)
  public static int allayHealInterval = 10;

  public static final class PositiveIntValidator extends Validator<Integer> {
    @Override
    public Integer validate(
        CommandSourceStack source, CarpetRule<Integer> rule, Integer newValue, String userInput) {
      if (newValue == null || newValue < 1) {
        return null;
      }
      return newValue;
    }

    @Override
    public String description() {
      return CarpetLmsTranslations.translate("carpet.validator.positiveInt");
    }
  }
}
