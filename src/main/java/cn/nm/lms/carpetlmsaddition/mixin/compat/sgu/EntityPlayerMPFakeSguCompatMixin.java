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
package cn.nm.lms.carpetlmsaddition.mixin.compat.sgu;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import carpet.patches.EntityPlayerMPFake;

import cn.nm.lms.carpetlmsaddition.bot.FakePlayerSpawner;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;

/**
 * Compatibility mixin for carpet-sgu-addition.
 *
 * <p>
 * When SGU's {@code betterFakePlayerProcess} rule is enabled, SGU's {@code @WrapMethod} on {@code createFake} bypasses
 * the original method body (not calling {@code original.call()}), which means LMS's existing {@code @WrapOperation} on
 * the original body's {@code whenCompleteAsync} is never reached.
 * </p>
 *
 * <p>
 * This mixin directly targets SGU's handler method {@code betterCreateFake} (merged onto {@code EntityPlayerMPFake}
 * after SGU's mixin is applied) and wraps its {@code whenCompleteAsync} call to propagate the silence scope into the
 * async callback.
 * </p>
 */
@Restriction(require = @Condition("carpet-sgu-addition"))
@Mixin(value = EntityPlayerMPFake.class, priority = 1500, remap = false)
public class EntityPlayerMPFakeSguCompatMixin {
    @Dynamic("Method betterCreateFake is added by carpet-sgu-addition")
    @WrapOperation(method = "betterCreateFake", remap = false, at = @At(value = "INVOKE", remap = false,
        target = "Ljava/util/concurrent/CompletableFuture;whenCompleteAsync(Ljava/util/function/BiConsumer;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static <T> CompletableFuture<T> carpetlmsaddition$propagateSilenceToSguAsync(CompletableFuture<T> instance,
        BiConsumer<? super T, ? super Throwable> action, Executor executor, Operation<CompletableFuture<T>> original) {
        boolean silence = FakePlayerSpawner.isSilenceEnabled();
        BiConsumer<? super T, ? super Throwable> wrapped =
            (value, throwable) -> FakePlayerSpawner.runWithSilenceScope(silence, () -> action.accept(value, throwable));
        return original.call(instance, wrapped, executor);
    }
}
