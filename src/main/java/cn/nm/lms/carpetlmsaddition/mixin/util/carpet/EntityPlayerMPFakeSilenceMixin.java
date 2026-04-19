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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

import net.minecraft.core.UUIDUtil;
import net.minecraft.server.TickTask;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import carpet.patches.EntityPlayerMPFake;

import cn.nm.lms.carpetlmsaddition.bot.FakePlayerSpawner;

@Mixin(EntityPlayerMPFake.class)
public class EntityPlayerMPFakeSilenceMixin {
    //#if MC>=12104
    @WrapOperation(method = "createFake", at = @At(value = "INVOKE",
        target = "Ljava/util/concurrent/CompletableFuture;whenCompleteAsync(Ljava/util/function/BiConsumer;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static <T> CompletableFuture<T> carpetlmsaddition$propagateSilenceToAsync(CompletableFuture<T> instance,
        BiConsumer<? super T, ? super Throwable> action, Executor executor, Operation<CompletableFuture<T>> original) {
        boolean silence = FakePlayerSpawner.isSilenceEnabled();
        BiConsumer<? super T, ? super Throwable> wrapped =
            (value, throwable) -> FakePlayerSpawner.runWithSilenceScope(silence, () -> action.accept(value, throwable));
        return original.call(instance, wrapped, executor);
    }
    //#else
    //$$ @WrapOperation(method = "createFake", at = @At(value = "INVOKE",
    //$$     target = "Ljava/util/concurrent/CompletableFuture;thenAcceptAsync(Ljava/util/function/Consumer;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    //$$ private static <T> CompletableFuture<Void> carpetlmsaddition$propagateSilenceToAsync(CompletableFuture<T> instance,
    //$$     java.util.function.Consumer<? super T> action, Executor executor, Operation<CompletableFuture<Void>> original) {
    //$$     boolean silence = FakePlayerSpawner.isSilenceEnabled();
    //$$     java.util.function.Consumer<? super T> wrapped =
    //$$         value -> FakePlayerSpawner.runWithSilenceScope(silence, () -> action.accept(value));
    //$$     return original.call(instance, wrapped, executor);
    //$$ }
    //#endif

    //#if MC>=12110
    @WrapOperation(method = "createFake", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/players/OldUsersConverter;convertMobOwnerIfNecessary(Lnet/minecraft/server/MinecraftServer;Ljava/lang/String;)Ljava/util/UUID;"))
    private static java.util.UUID carpetlmsaddition$skipOnlineLookupForSilentSpawn(
        net.minecraft.server.MinecraftServer server, String username, Operation<java.util.UUID> original) {
        if (FakePlayerSpawner.isSilenceEnabled()) {
            return UUIDUtil.createOfflinePlayerUUID(username);
        }
        return original.call(server, username);
    }
    //#else
    //$$ @WrapOperation(method = "createFake", at = @At(value = "INVOKE",
    //$$     target = "Lnet/minecraft/server/players/GameProfileCache;get(Ljava/lang/String;)Ljava/util/Optional;"))
    //$$ private static java.util.Optional<com.mojang.authlib.GameProfile> carpetlmsaddition$skipOnlineLookupForSilentSpawn(
    //$$     net.minecraft.server.players.GameProfileCache instance, String username,
    //$$     Operation<java.util.Optional<com.mojang.authlib.GameProfile>> original) {
    //$$     if (FakePlayerSpawner.isSilenceEnabled()) {
    //$$         return java.util.Optional.of(new com.mojang.authlib.GameProfile(UUIDUtil.createOfflinePlayerUUID(username), username));
    //$$     }
    //$$     return original.call(instance, username);
    //$$ }
    //#endif

    @WrapOperation(method = "kill(Lnet/minecraft/network/chat/Component;)V",
        at = @At(value = "NEW", target = "(ILjava/lang/Runnable;)Lnet/minecraft/server/TickTask;"))
    private TickTask carpetlmsaddition$propagateSilenceToKillTask(int tick, Runnable runnable,
        Operation<TickTask> original) {
        boolean silence = FakePlayerSpawner.isSilenceEnabled();
        return original.call(tick, (Runnable)() -> FakePlayerSpawner.runWithSilenceScope(silence, runnable));
    }
}
