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
package cn.nm.lms.carpetlmsaddition.rule.util.command.lms;

import java.util.function.BooleanSupplier;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import cn.nm.lms.carpetlmsaddition.lib.PlayerConfig;

final class LmsBooleanConfigCommand implements LmsConfigCommand {
    private final String config;
    private final BooleanSupplier enabled;

    LmsBooleanConfigCommand(String config) {
        this(config, () -> true);
    }

    LmsBooleanConfigCommand(String config, BooleanSupplier enabled) {
        this.config = config;
        this.enabled = enabled;
    }

    @Override
    public boolean enabled() {
        return enabled.getAsBoolean();
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext _commandBuildContext) {
        return Commands.literal(config).executes(this::executeGet)
            .then(Commands.argument(CommandLms.ARG_VALUE, BoolArgumentType.bool()).executes(this::executeSet));
    }

    private int executeGet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer target = CommandLms.getTarget(ctx);
        if (CommandLms.cannotUse(src, target)) {
            CommandLms.sendNoPermission(src);
            return 0;
        }

        Boolean raw = PlayerConfig.getBoolean(target.getUUID(), config);
        CommandLms.sendConfigValue(src, config, raw == null ? "null" : raw.toString());
        return 1;
    }

    private int executeSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer target = CommandLms.getTarget(ctx);
        if (CommandLms.cannotUse(src, target)) {
            CommandLms.sendNoPermission(src);
            return 0;
        }

        boolean value = BoolArgumentType.getBool(ctx, CommandLms.ARG_VALUE);
        PlayerConfig.setBoolean(target.getUUID(), config, value);
        CommandLms.sendConfigValue(src, config, Boolean.toString(value));
        return 1;
    }
}
