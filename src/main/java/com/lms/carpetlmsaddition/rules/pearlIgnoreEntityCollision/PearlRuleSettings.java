package com.lms.carpetlmsaddition.rules.pearlIgnoreEntityCollision;

import static carpet.api.settings.RuleCategory.SURVIVAL;

import carpet.api.settings.Rule;
import com.lms.carpetlmsaddition.lib.RuleSupport;

public final class PearlRuleSettings {
  private PearlRuleSettings() {}

  @Rule(categories = {RuleSupport.LMS, SURVIVAL})
  public static boolean pearlIgnoreEntityCollision = false;
}
