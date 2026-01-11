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
package cn.nm.lms.carpetlmsaddition.mixin.opplayernocheatextra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import cn.nm.lms.carpetlmsaddition.rules.opplayernocheatextra.OpPlayerNoCheatExtraHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.PlaceCommand;

import java.util.function.Predicate;

@Mixin(PlaceCommand.class)
public abstract class OpPlayerNoCheatExtraPlaceMixin
{
    @WrapOperation(
            method = "register", at = @At(
                    value = "INVOKE", target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;", remap = false), require = 1, allow = 1)
    private static ArgumentBuilder<CommandSourceStack, ?> opPlayerNoCheatExtra$wrapRequires(
                                                                                            LiteralArgumentBuilder<CommandSourceStack> instance, Predicate<CommandSourceStack> predicate, Operation<ArgumentBuilder<CommandSourceStack, ?>> original)
    {
        return original.call(instance, OpPlayerNoCheatExtraHelper.wrapPredicate(predicate));
    }
}
