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
package cn.nm.lms.carpetlmsaddition.rule.block.breaking.restriction;

import java.util.Set;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import cn.nm.lms.carpetlmsaddition.lib.BlockRegistryCompat;
import cn.nm.lms.carpetlmsaddition.lib.PlayerConfig;

public class PlayerRestriction {
    public static final String CONFIG_MODE = "blockRestrictionMode";
    public static final String CONFIG_BLACKLIST = "blockRestrictionBlacklist";
    public static final String CONFIG_WHITELIST = "blockRestrictionWhitelist";

    private final ServerPlayer player;
    private final String blockId;

    private PlayerRestriction(ServerPlayer player, Block block) {
        this.player = player;
        this.blockId = BlockRegistryCompat.getBlockId(block);
    }

    public static boolean shouldLimit(ServerPlayer player, Block block) {
        return new PlayerRestriction(player, block).shouldLimit();
    }

    public static boolean shouldLimit(ServerPlayer player, BlockState state) {
        return shouldLimit(player, state.getBlock());
    }

    private boolean inBlockSet(String configName) {
        Set<String> blockIds = PlayerConfig.getStringSet(player.getUUID(), configName);
        if (blockIds == null) {
            return false;
        }
        return blockIds.contains(this.blockId);
    }

    private boolean shouldLimit() {
        Mode mode = PlayerConfig.getEnum(player.getUUID(), CONFIG_MODE, Mode.class);
        if (mode == null) {
            return false;
        }
        return switch (mode) {
            case BLACKLIST -> inBlockSet(CONFIG_BLACKLIST);
            case WHITELIST -> !inBlockSet(CONFIG_WHITELIST);
            default -> false;
        };
    }

    public enum Mode {
        OFF, BLACKLIST, WHITELIST
    }
}
