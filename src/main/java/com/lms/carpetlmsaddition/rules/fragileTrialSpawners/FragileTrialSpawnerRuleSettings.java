package com.lms.carpetlmsaddition.rules.fragileTrialSpawners;

import static carpet.api.settings.RuleCategory.SURVIVAL;

import carpet.api.settings.Rule;
import com.lms.carpetlmsaddition.lib.RuleSupport;

public final class FragileTrialSpawnerRuleSettings {
  private FragileTrialSpawnerRuleSettings() {}

  @Rule(categories = {RuleSupport.LMS, SURVIVAL})
  public static boolean fragileTrialSpawners = false;
}
