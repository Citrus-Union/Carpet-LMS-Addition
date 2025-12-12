package com.lms.carpetlmsaddition;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import net.minecraft.commands.CommandSourceStack;

public class PositiveIntValidator extends Validator<Integer> {
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
    return "Value must be at least 1 tick.";
  }
}
