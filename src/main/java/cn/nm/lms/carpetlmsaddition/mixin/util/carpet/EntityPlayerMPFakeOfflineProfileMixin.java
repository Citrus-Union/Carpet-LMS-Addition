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
package cn.nm.lms.carpetlmsaddition.mixin.util.carpet;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import carpet.patches.EntityPlayerMPFake;

@Mixin(EntityPlayerMPFake.class)
public class EntityPlayerMPFakeOfflineProfileMixin {
    //#if MC>=12110
    @WrapOperation(method = "createFake", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/players/OldUsersConverter;convertMobOwnerIfNecessary(Lnet/minecraft/server/MinecraftServer;Ljava/lang/String;)Ljava/util/UUID;"))
    private static UUID carpetlmsaddition$skipOnlineLookupForOfflineProfile(MinecraftServer server, String username,
        Operation<UUID> original) {
        return UUIDUtil.createOfflinePlayerUUID(username);
    }

    @WrapOperation(method = "fetchGameProfile", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/component/ResolvableProfile;resolveProfile(Lnet/minecraft/server/players/ProfileResolver;)Ljava/util/concurrent/CompletableFuture;"))
    private static CompletableFuture<GameProfile> carpetlmsaddition$skipOnlineSkinLookupForOfflineProfile(
        ResolvableProfile instance, net.minecraft.server.players.ProfileResolver resolver,
        Operation<CompletableFuture<GameProfile>> original) {
        return CompletableFuture.completedFuture(instance.partialProfile());
    }

    @Unique
    private static void carpetlmsaddition$keepLegacyImports() {
        Optional.empty();
        SkullBlockEntity.class.getName();
    }
    //#else
    //$$ @WrapOperation(method = "createFake", at = @At(value = "INVOKE",
    //$$     target = "Lnet/minecraft/server/players/GameProfileCache;get(Ljava/lang/String;)Ljava/util/Optional;"))
    //$$ private static Optional<GameProfile> carpetlmsaddition$skipOnlineLookupForOfflineProfile(
    //$$     net.minecraft.server.players.GameProfileCache instance, String username,
    //$$     Operation<Optional<GameProfile>> original) {
    //$$     return Optional.of(new GameProfile(UUIDUtil.createOfflinePlayerUUID(username), username));
    //$$ }
    //$$
    //$$ @WrapOperation(method = "fetchGameProfile", at = @At(value = "INVOKE",
    //$$     target = "Lnet/minecraft/world/level/block/entity/SkullBlockEntity;fetchGameProfile(Ljava/lang/String;)Ljava/util/concurrent/CompletableFuture;"))
    //$$ private static CompletableFuture<Optional<GameProfile>> carpetlmsaddition$skipOnlineSkinLookupForOfflineProfile(
    //$$     String username,
    //$$     Operation<CompletableFuture<Optional<GameProfile>>> original) {
    //$$     return CompletableFuture.completedFuture(
    //$$         Optional.of(new GameProfile(UUIDUtil.createOfflinePlayerUUID(username), username)));
    //$$ }
    //#endif
}
