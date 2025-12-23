package cn.nm.lms.carpetlmsaddition.rules.renewableelytra

import carpet.api.settings.Rule
import carpet.api.settings.RuleCategory
import cn.nm.lms.carpetlmsaddition.rules.LMSRuleCategory

object RenewableElytra {
    @Rule(
        categories = [LMSRuleCategory.LMS, RuleCategory.SURVIVAL],
    )
    @JvmField
    var renewableElytra: Boolean = false
}
