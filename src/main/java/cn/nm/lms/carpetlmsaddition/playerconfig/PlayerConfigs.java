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
package cn.nm.lms.carpetlmsaddition.playerconfig;

import java.util.List;

import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.block.breaking.restriction.PlayerRestriction;

public final class PlayerConfigs {
    public static final BooleanPlayerConfig LOW_HEALTH_SPECTATOR = PlayerConfigTypes.bool("lowHealthSpectator",
        () -> Settings.lowHealthSpectator == PlayerConfigStore.RuleSetting.CUSTOM);
    public static final EnumPlayerConfig<PlayerRestriction.Mode> BLOCK_RESTRICTION_MODE = PlayerConfigTypes
        .enumConfig("blockRestrictionMode", PlayerRestriction.Mode.class, () -> Settings.breakingRestriction);
    public static final BlockSetPlayerConfig BLOCK_RESTRICTION_BLACKLIST =
        PlayerConfigTypes.blockSet("blockRestrictionBlacklist", () -> Settings.breakingRestriction);
    public static final BlockSetPlayerConfig BLOCK_RESTRICTION_WHITELIST =
        PlayerConfigTypes.blockSet("blockRestrictionWhitelist", () -> Settings.breakingRestriction);

    public static final List<PlayerConfigEntry<?>> ALL =
        List.of(LOW_HEALTH_SPECTATOR, BLOCK_RESTRICTION_MODE, BLOCK_RESTRICTION_BLACKLIST, BLOCK_RESTRICTION_WHITELIST);

    private PlayerConfigs() {}
}
