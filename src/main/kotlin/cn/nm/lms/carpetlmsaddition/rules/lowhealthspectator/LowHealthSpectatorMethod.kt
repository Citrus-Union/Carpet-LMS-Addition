package cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator

import carpet.api.settings.Rule
import carpet.api.settings.RuleCategory
import cn.nm.lms.carpetlmsaddition.rules.LMSRuleCategory

object LowHealthSpectatorMethod {
    @Rule(
        categories = [LMSRuleCategory.LMS, RuleCategory.SURVIVAL],
        options = ["vanilla", "mcdreforged", "carpet"],
    )
    @JvmField
    var lowHealthSpectatorMethod: String = "vanilla"
}
