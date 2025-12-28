package cn.nm.lms.carpetlmsaddition.rules.minimallootinglevel

import carpet.api.settings.Rule
import carpet.api.settings.RuleCategory
import carpet.api.settings.Validators
import cn.nm.lms.carpetlmsaddition.rules.LMSRuleCategory

object MinimalLootingLevel {
    @Rule(
        categories = [LMSRuleCategory.LMS, RuleCategory.CREATIVE],
        validators = [Validators.NonNegativeNumber::class],
    )
    @JvmField
    var minimalLootingLevel: Int = 0
}
