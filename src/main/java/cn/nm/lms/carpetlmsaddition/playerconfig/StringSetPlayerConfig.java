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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.playerconfig.command.PlayerConfigCommandSupport;

public class StringSetPlayerConfig extends PlayerConfigEntry<Set<String>> {
    protected final String argumentName;

    StringSetPlayerConfig(String key, String argumentName, BooleanSupplier enabled) {
        super(key, enabled);
        this.argumentName = argumentName;
    }

    @Override
    public @Nullable Set<String> get(UUID playerUUID) {
        return PlayerConfigStore.getStringSet(playerUUID, key);
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext commandBuildContext) {
        RequiredArgumentBuilder<CommandSourceStack, ?> addValueArg = addValueArgument(commandBuildContext);
        RequiredArgumentBuilder<CommandSourceStack, String> removeValueArg = valueArgument();
        return Commands.literal(key).executes(this::executeGet)
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
        try {
            Set<String> values = get(PlayerConfigCommandSupport.getTarget(ctx).getUUID());
            if (values != null) {
                values.forEach(builder::suggest);
            }
        } catch (CommandSyntaxException e) {
            return builder.buildFuture();
        }
        return builder.buildFuture();
    }

    private int executeAdd(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withPermittedTarget(ctx, (src, target) -> {
            String value = normalizeAddValue(ctx, src);
            if (value == null) {
                return 0;
            }
            Set<String> values = currentValuesOrEmpty(target.getUUID());
            values.add(value);
            PlayerConfigStore.setStringSet(target.getUUID(), key, values);
            PlayerConfigCommandSupport.sendConfigValue(src, key, values.toString());
            return 1;
        });
    }

    @Nullable
    protected String normalizeAddValue(CommandContext<CommandSourceStack> ctx, CommandSourceStack src) {
        return StringArgumentType.getString(ctx, argumentName);
    }

    private int executeRemove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String value = StringArgumentType.getString(ctx, argumentName);
        return withPermittedTarget(ctx, (src, target) -> {
            Set<String> values = get(target.getUUID());
            if (values == null || !values.contains(value)) {
                PlayerConfigCommandSupport.sendFailure(src,
                    PlayerConfigCommandSupport.MESSAGE_VALUE_NOT_FOUND_PREFIX + value);
                return 0;
            }
            values = new LinkedHashSet<>(values);
            values.remove(value);
            PlayerConfigStore.setStringSet(target.getUUID(), key, values);
            PlayerConfigCommandSupport.sendConfigValue(src, key, values.toString());
            return 1;
        });
    }

    private int executeClear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withPermittedTarget(ctx, (src, target) -> {
            Set<String> values = Set.of();
            PlayerConfigStore.setStringSet(target.getUUID(), key, values);
            PlayerConfigCommandSupport.sendConfigValue(src, key, values.toString());
            return 1;
        });
    }

    private Set<String> currentValuesOrEmpty(UUID playerUUID) {
        Set<String> values = get(playerUUID);
        return values == null ? new LinkedHashSet<>() : new LinkedHashSet<>(values);
    }
}
