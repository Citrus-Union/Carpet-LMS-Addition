package cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator

import carpet.api.settings.Rule
import carpet.api.settings.RuleCategory
import carpet.api.settings.Validators
import cn.nm.lms.carpetlmsaddition.rules.LMSRuleCategory

object LowHealthSpectatorThreshold {
    @Rule(
        categories = [LMSRuleCategory.LMS, RuleCategory.SURVIVAL],
        validators = [Validators.NonNegativeNumber::class],
    )
    @JvmField
    var lowHealthSpectatorThreshold: Int = 5
}
