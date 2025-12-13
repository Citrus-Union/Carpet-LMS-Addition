package com.lms.carpetlmsaddition.rules.fragileVaults;

import static carpet.api.settings.RuleCategory.SURVIVAL;

import carpet.api.settings.Rule;
import com.lms.carpetlmsaddition.lib.RuleSupport;

public final class FragileVaultRuleSettings {
  private FragileVaultRuleSettings() {}

  @Rule(categories = {RuleSupport.LMS, SURVIVAL})
  public static boolean fragileVaults = false;
}
