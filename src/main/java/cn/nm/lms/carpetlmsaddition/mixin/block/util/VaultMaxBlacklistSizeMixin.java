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
package cn.nm.lms.carpetlmsaddition.mixin.block.util;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.vault.VaultServerData;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import cn.nm.lms.carpetlmsaddition.rule.Settings;

@Mixin(VaultServerData.class)
public abstract class VaultMaxBlacklistSizeMixin {
    @Shadow
    @Final
    private Set<UUID> rewardedPlayers;

    @Shadow
    protected abstract void markChanged();

    @WrapMethod(method = "addToRewardedPlayers")
    public void wAddToRewardedPlayers(final Player player, Operation<Void> original) {

        if (Settings.vaultMaxBlacklistSize < 0) {
            original.call(player);
            return;
        }
        this.rewardedPlayers.add(player.getUUID());
        if (this.rewardedPlayers.size() > Settings.vaultMaxBlacklistSize) {
            Iterator<UUID> iterator = this.rewardedPlayers.iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }

        this.markChanged();
    }
}
