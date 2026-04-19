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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.level.storage.LevelResource;

import org.jspecify.annotations.NonNull;

public final class OfflineInvCheck {
    private OfflineInvCheck() {}

    static boolean isMainInvAndHotbarEmpty(MinecraftServer server, String name) {
        UUID uuid = resolve(server, name);
        Path playerDat = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(uuid.toString() + ".dat");
        if (!Files.exists(playerDat)) {
            return true;
        }

        try {
            CompoundTag tag = NbtIo.readCompressed(playerDat, NbtAccounter.unlimitedHeap());
            //#if MC>=12105
            ListTag inv = tag.getList("Inventory").orElse(new ListTag());
            //#else
            //$$ ListTag inv = tag.getList("Inventory", net.minecraft.nbt.Tag.TAG_COMPOUND);
            //#endif
            for (int i = 0; i < inv.size(); i++) {
                //#if MC>=12105
                CompoundTag one = inv.getCompound(i).orElse(new CompoundTag());
                int slot = one.getByte("Slot").orElse((byte)-1) & 255;
                String itemId = one.getString("id").orElse("minecraft:air");
                //#else
                //$$ CompoundTag one = inv.getCompound(i);
                //$$ int slot = one.getByte("Slot") & 255;
                //$$ String itemId = one.getString("id");
                //#endif
                if (slot <= 35 && !"minecraft:air".equals(itemId)) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            return true;
        }
    }

    @NonNull
    private static UUID resolve(MinecraftServer server, @NonNull String name) {
        UUID uuid = OldUsersConverter.convertMobOwnerIfNecessary(server, name);
        if (uuid != null) {
            return uuid;
        }
        return UUIDUtil.createOfflinePlayerUUID(name);
    }
}
