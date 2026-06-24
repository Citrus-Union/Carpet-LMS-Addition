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

import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.context.CommandContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import carpet.commands.PlayerCommand;
import carpet.helpers.EntityPlayerActionPack;

import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.util.command.CommandUtils;

@Mixin(PlayerCommand.class)
public abstract class PlayerCommandMixin {
    @Inject(method = "manipulate", remap = false,
        at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"),
        cancellable = true)
    private static void spectatorPlayerCannotDropItem$lms(CommandContext<CommandSourceStack> context,
        Consumer<EntityPlayerActionPack> action, CallbackInfoReturnable<Integer> cir, @Local ServerPlayer player) {
        if (!Settings.spectatorPlayerCannotDropItem || !player.isSpectator() || !lms$isDropCommand(context)) {
            return;
        }

        CommandUtils.multiple(context.getSource(), "spectatorPlayerCannotDropItem", 2, false);
        cir.setReturnValue(0);
    }

    @Unique
    private static final Set<String> DROP_COMMANDS = Set.of("drop", "dropStack", "dropall");

    @Unique
    private static boolean lms$isDropCommand(CommandContext<CommandSourceStack> context) {
        if (context.getNodes().size() <= 2) {
            return false;
        }

        String name = context.getNodes().get(2).getNode().getName();
        return DROP_COMMANDS.contains(name);
    }
}
