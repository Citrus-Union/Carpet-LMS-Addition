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
package cn.nm.lms.carpetlmsaddition.rule.util.command.lms;

import java.util.List;

import cn.nm.lms.carpetlmsaddition.lib.PlayerConfig;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.block.breaking.restriction.PlayerRestriction;

public final class CommandLmsImpl implements CommandLms {
    private static final List<LmsConfigCommand> CONFIG_COMMANDS = List.of(
        new LmsBooleanConfigCommand("lowHealthSpectator",
            () -> Settings.lowHealthSpectator == PlayerConfig.RuleSetting.CUSTOM),
        new LmsEnumConfigCommand<>(PlayerRestriction.CONFIG_MODE, PlayerRestriction.Mode.class,
            () -> Settings.breakingRestriction),
        new LmsBlockStringSetConfigCommand(PlayerRestriction.CONFIG_BLACKLIST, () -> Settings.breakingRestriction),
        new LmsBlockStringSetConfigCommand(PlayerRestriction.CONFIG_WHITELIST, () -> Settings.breakingRestriction));

    @Override
    public List<LmsConfigCommand> configCommands() {
        return CONFIG_COMMANDS;
    }
}
