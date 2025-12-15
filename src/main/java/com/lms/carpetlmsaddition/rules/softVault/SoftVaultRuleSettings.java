package com.lms.carpetlmsaddition.rules.softVault;

import carpet.api.settings.Rule;
import com.lms.carpetlmsaddition.lib.RuleSupport;

import static carpet.api.settings.RuleCategory.SURVIVAL;

public class SoftVaultRuleSettings {
    @Rule(categories = {RuleSupport.LMS, SURVIVAL})
    public static boolean softVault = false;

    private SoftVaultRuleSettings() {
    }
}
