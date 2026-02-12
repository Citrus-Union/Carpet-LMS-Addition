/*
 * Copyright (C) 2025  Carpet-LMS-Addition contributors
 * https://github.com/Citrus-Union/Carpet-LMS-Addition

 * Carpet LMS Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.

 * Carpet LMS Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Carpet LMS Addition.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.nm.lms.carpetlmsaddition.rule.block.util.dispenserbartering;

import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;

import cn.nm.lms.carpetlmsaddition.rule.LMSRuleCategory;

public final class DispenserBarteringRule
{
    @Rule(
            categories = {
                    LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE
            },
            options = {
                    "false", "ingot", "block", "shulkerBox"
            }
    )
    public static String dispenserBartering = "false";

    @Rule(
            categories = {
                    LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE
            },
            options = {
                    "true", "false"
            },
            strict = false
    )
    public static String dispenserBarteringName = "false";

    public static int getDispenserBarteringLevel()
    {
        return switch (dispenserBartering)
        {
            case "ingot" -> 1;
            case "block" -> 2;
            case "shulkerBox" -> 3;
            default -> 0;
        };
    }
}
