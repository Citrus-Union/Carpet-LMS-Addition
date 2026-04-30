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
import java.util.function.Consumer;

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
    private static <T> CompletableFuture<T> carpetlmsaddition$propagateSilenceToAsyncSpawn(
        CompletableFuture<T> instance, BiConsumer<? super T, ? super Throwable> action, Executor executor,
        Operation<CompletableFuture<T>> original) {
        boolean silence = FakePlayerSpawner.isSilenceEnabled();
        BiConsumer<? super T, ? super Throwable> wrapped =
            (value, throwable) -> FakePlayerSpawner.runWithSilenceScope(silence, () -> action.accept(value, throwable));
        return original.call(instance, wrapped, executor);
    }

    @org.spongepowered.asm.mixin.Unique
    private static void carpetlmsaddition$keepLegacyImports() {
        Consumer.class.getName();
    }
    //#else
    //$$ @WrapOperation(method = "createFake", at = @At(value = "INVOKE",
    //$$     target = "Ljava/util/concurrent/CompletableFuture;thenAcceptAsync(Ljava/util/function/Consumer;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    //$$ private static <T> CompletableFuture<Void> carpetlmsaddition$propagateSilenceToAsyncSpawn(
    //$$     CompletableFuture<T> instance, Consumer<? super T> action, Executor executor,
    //$$     Operation<CompletableFuture<Void>> original) {
    //$$     boolean silence = FakePlayerSpawner.isSilenceEnabled();
    //$$     Consumer<? super T> wrapped =
    //$$         value -> FakePlayerSpawner.runWithSilenceScope(silence, () -> action.accept(value));
    //$$     return original.call(instance, wrapped, executor);
    //$$ }
    //#endif

}
