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
package cn.nm.lms.carpetlmsaddition.mixin.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import org.slf4j.Logger;

import cn.nm.lms.carpetlmsaddition.bot.FakePlayerSpawner;

@Mixin(PlayerList.class)
public class FakePlayerSilencePlayerListMixin {
    @Inject(method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("HEAD"),
        cancellable = true)
    private void carpetlmsaddition$silenceBroadcast(Component message, boolean overlay, CallbackInfo ci) {
        if (FakePlayerSpawner.isSilenceEnabled()) {
            ci.cancel();
        }
    }

    @WrapWithCondition(method = "placeNewPlayer", at = @At(value = "INVOKE", remap = false,
        target = "Lorg/slf4j/Logger;info(Ljava/lang/String;[Ljava/lang/Object;)V"))
    private boolean carpetlmsaddition$silenceLoginLog(Logger instance, String format, Object[] args) {
        return !FakePlayerSpawner.isSilenceEnabled();
    }
}
