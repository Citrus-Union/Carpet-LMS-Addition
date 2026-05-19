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

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import cn.nm.lms.carpetlmsaddition.lib.PlayerConfig;

final class LmsEnumConfigCommand<E extends Enum<E>> implements LmsConfigCommand {
    private final String config;
    private final Class<E> enumClass;
    private final BooleanSupplier enabled;

    LmsEnumConfigCommand(String config, Class<E> enumClass) {
        this(config, enumClass, () -> true);
    }

    LmsEnumConfigCommand(String config, Class<E> enumClass, BooleanSupplier enabled) {
        this.config = config;
        this.enumClass = enumClass;
        this.enabled = enabled;
    }

    @Override
    public boolean enabled() {
        return enabled.getAsBoolean();
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext _commandBuildContext) {
        RequiredArgumentBuilder<CommandSourceStack, String> valueArg =
            Commands.argument(CommandLms.ARG_VALUE, StringArgumentType.word()).suggests((ctx, builder) -> {
                for (E value : enumClass.getEnumConstants()) {
                    builder.suggest(CommandLms.enumValue(value));
                }
                return builder.buildFuture();
            }).executes(this::executeSet);
        return Commands.literal(config).executes(this::executeGet).then(valueArg);
    }

    private int executeGet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer target = CommandLms.getTarget(ctx);
        if (CommandLms.cannotUse(src, target)) {
            CommandLms.sendNoPermission(src);
            return 0;
        }

        String raw = PlayerConfig.get(target.getUUID(), config);
        CommandLms.sendConfigValue(src, config, raw == null ? "null" : raw);
        return 1;
    }

    private int executeSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer target = CommandLms.getTarget(ctx);
        String value = CommandLms.getValue(ctx);
        E enumValue = CommandLms.parseEnum(value, enumClass);
        if (enumValue == null) {
            CommandLms.sendFailure(src, CommandLms.MESSAGE_UNKNOWN_VALUE_PREFIX + value);
            return 0;
        }
        if (CommandLms.cannotUse(src, target)) {
            CommandLms.sendNoPermission(src);
            return 0;
        }

        PlayerConfig.setEnum(target.getUUID(), config, enumValue);
        CommandLms.sendConfigValue(src, config, CommandLms.enumValue(enumValue));
        return 1;
    }
}
