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
package cn.nm.lms.carpetlmsaddition.mixin.util.command.playercommanddropall;

import java.util.function.Consumer;

import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.context.CommandContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import carpet.commands.PlayerCommand;
import carpet.helpers.EntityPlayerActionPack;

@Mixin(value = PlayerCommand.class, remap = false)
public interface PlayerCommandDropallMixin {
    @Invoker(value = "manipulate", remap = false)
    static int invokeManipulate$LMS(CommandContext<CommandSourceStack> context, Consumer<EntityPlayerActionPack> op) {
        throw new AssertionError();
    }
}
