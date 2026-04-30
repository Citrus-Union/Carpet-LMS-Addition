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
package cn.nm.lms.carpetlmsaddition.bot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import cn.nm.lms.carpetlmsaddition.lib.PlayerUtils;
import cn.nm.lms.carpetlmsaddition.lib.getvalue.GetPaths;

public final class OfflineInvCheck {
    private OfflineInvCheck() {}

    static boolean isInventoryEmpty(MinecraftServer server, String name) {
        UUID uuid = UUIDUtil.createOfflinePlayerUUID(name);
        Path playerDat = GetPaths.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(uuid.toString() + ".dat");
        if (!Files.exists(playerDat)) {
            return true;
        }

        try {
            return PlayerUtils.isInventoryEmpty(playerDat);
        } catch (IOException e) {
            return true;
        }
    }
}
