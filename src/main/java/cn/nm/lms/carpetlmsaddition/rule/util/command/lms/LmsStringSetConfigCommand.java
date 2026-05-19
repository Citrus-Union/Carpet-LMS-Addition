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

import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.lib.PlayerConfig;

class LmsStringSetConfigCommand implements LmsConfigCommand {
    protected final String config;
    protected final String argumentName;
    private final BooleanSupplier enabled;

    LmsStringSetConfigCommand(String config, String argumentName) {
        this(config, argumentName, () -> true);
    }

    LmsStringSetConfigCommand(String config, String argumentName, BooleanSupplier enabled) {
        this.config = config;
        this.argumentName = argumentName;
        this.enabled = enabled;
    }

    @Override
    public boolean enabled() {
        return enabled.getAsBoolean();
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext commandBuildContext) {
        RequiredArgumentBuilder<CommandSourceStack, ?> addValueArg = addValueArgument(commandBuildContext);
        RequiredArgumentBuilder<CommandSourceStack, String> removeValueArg = valueArgument();
        return Commands.literal(config).executes(this::executeGet)
            .then(Commands.literal("add").then(addValueArg.executes(this::executeAdd)))
            .then(Commands.literal("remove").then(removeValueArg.executes(this::executeRemove)))
            .then(Commands.literal("clear").executes(this::executeClear));
    }

    protected RequiredArgumentBuilder<CommandSourceStack, ?>
        addValueArgument(CommandBuildContext _commandBuildContext) {
        return Commands.argument(argumentName, StringArgumentType.greedyString());
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> valueArgument() {
        return Commands.argument(argumentName, StringArgumentType.greedyString()).suggests(this::suggestRemoveValues);
    }

    protected CompletableFuture<Suggestions> suggestRemoveValues(CommandContext<CommandSourceStack> ctx,
        SuggestionsBuilder builder) {
        ServerPlayer target;
        try {
            target = CommandLms.getTarget(ctx);
        } catch (CommandSyntaxException e) {
            return builder.buildFuture();
        }
        Set<String> values = PlayerConfig.getStringSet(target.getUUID(), config);
        if (values != null) {
            values.forEach(builder::suggest);
        }
        return builder.buildFuture();
    }

    private int executeGet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer target = CommandLms.getTarget(ctx);
        if (CommandLms.cannotUse(src, target)) {
            CommandLms.sendNoPermission(src);
            return 0;
        }

        Set<String> raw = PlayerConfig.getStringSet(target.getUUID(), config);
        CommandLms.sendConfigValue(src, config, raw == null ? "null" : raw.toString());
        return 1;
    }

    private int executeAdd(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer target = CommandLms.getTarget(ctx);
        if (CommandLms.cannotUse(src, target)) {
            CommandLms.sendNoPermission(src);
            return 0;
        }

        String value = normalizeAddValue(ctx, src);
        if (value == null) {
            return 0;
        }
        Set<String> values = PlayerConfig.addToStringSet(target.getUUID(), config, value);
        CommandLms.sendConfigValue(src, config, values.toString());
        return 1;
    }

    @Nullable
    protected String normalizeAddValue(CommandContext<CommandSourceStack> ctx, CommandSourceStack src) {
        return StringArgumentType.getString(ctx, argumentName);
    }

    private int executeRemove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer target = CommandLms.getTarget(ctx);
        if (CommandLms.cannotUse(src, target)) {
            CommandLms.sendNoPermission(src);
            return 0;
        }

        String value = StringArgumentType.getString(ctx, argumentName);
        Set<String> values = PlayerConfig.getStringSet(target.getUUID(), config);
        if (values == null || !values.contains(value)) {
            CommandLms.sendFailure(src, CommandLms.MESSAGE_VALUE_NOT_FOUND_PREFIX + value);
            return 0;
        }
        values = PlayerConfig.removeFromStringSet(target.getUUID(), config, value);
        CommandLms.sendConfigValue(src, config, values.toString());
        return 1;
    }

    private int executeClear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer target = CommandLms.getTarget(ctx);
        if (CommandLms.cannotUse(src, target)) {
            CommandLms.sendNoPermission(src);
            return 0;
        }

        Set<String> values = Set.of();
        PlayerConfig.setStringSet(target.getUUID(), config, values);
        CommandLms.sendConfigValue(src, config, values.toString());
        return 1;
    }
}
