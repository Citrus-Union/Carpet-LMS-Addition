package cn.nm.lms.carpetlmsaddition.rules

import carpet.api.settings.Rule
import carpet.api.settings.RuleCategory

object FragileVault {
    @Rule(
        categories = [LMSRuleCategory.LMS, RuleCategory.SURVIVAL],
    )
    @JvmField
    var fragileVault: Boolean = false
}
