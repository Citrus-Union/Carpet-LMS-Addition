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
package cn.nm.lms.carpetlmsaddition.playerconfig;

import java.util.Locale;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.playerconfig.command.PlayerConfigCommandSupport;

public final class EnumPlayerConfig<E extends Enum<E>> extends PlayerConfigEntry<E> {
    private final Class<E> enumClass;

    EnumPlayerConfig(String key, Class<E> enumClass, BooleanSupplier enabled) {
        super(key, enabled);
        this.enumClass = enumClass;
    }

    @Override
    public @Nullable E get(UUID playerUUID) {
        return PlayerConfigStore.getEnum(playerUUID, key, enumClass);
    }

    @Override
    protected String formatValue(@Nullable E value) {
        return value == null ? "null" : enumValue(value);
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext _commandBuildContext) {
        RequiredArgumentBuilder<CommandSourceStack, String> valueArg = Commands
            .argument(PlayerConfigCommandSupport.ARG_VALUE, StringArgumentType.word()).suggests((ctx, builder) -> {
                for (E value : enumClass.getEnumConstants()) {
                    builder.suggest(enumValue(value));
                }
                return builder.buildFuture();
            }).executes(this::executeSet);
        return Commands.literal(key).executes(this::executeGet).then(valueArg);
    }

    private int executeSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        String value = PlayerConfigCommandSupport.getValue(ctx);
        E enumValue = parseEnum(value);
        if (enumValue == null) {
            PlayerConfigCommandSupport.sendFailure(src,
                PlayerConfigCommandSupport.MESSAGE_UNKNOWN_VALUE_PREFIX + value);
            return 0;
        }
        return withPermittedTarget(ctx, (_src, target) -> {
            PlayerConfigStore.setEnum(target.getUUID(), key, enumValue);
            PlayerConfigCommandSupport.sendConfigValue(src, key, enumValue(enumValue));
            return 1;
        });
    }

    private static String enumValue(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }

    @Nullable
    private E parseEnum(String value) {
        try {
            return Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
