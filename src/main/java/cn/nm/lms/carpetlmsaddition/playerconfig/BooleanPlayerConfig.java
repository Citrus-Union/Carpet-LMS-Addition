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

import java.util.UUID;
import java.util.function.BooleanSupplier;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.playerconfig.command.PlayerConfigCommandSupport;

public final class BooleanPlayerConfig extends PlayerConfigEntry<Boolean> {
    BooleanPlayerConfig(String key, BooleanSupplier enabled) {
        super(key, enabled);
    }

    @Override
    public @Nullable Boolean get(UUID playerUUID) {
        return PlayerConfigStore.getBoolean(playerUUID, key);
    }

    public boolean isPlayerEnabled(PlayerConfigStore.RuleSetting setting, UUID playerUUID) {
        return switch (setting) {
            case TRUE -> true;
            case FALSE -> false;
            case CUSTOM -> isEnabledFor(playerUUID);
        };
    }

    private boolean isEnabledFor(UUID playerUUID) {
        return Boolean.TRUE.equals(get(playerUUID));
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext _commandBuildContext) {
        return Commands.literal(key).executes(this::executeGet).then(Commands
            .argument(PlayerConfigCommandSupport.ARG_VALUE, BoolArgumentType.bool()).executes(this::executeSet));
    }

    private int executeSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        boolean value = BoolArgumentType.getBool(ctx, PlayerConfigCommandSupport.ARG_VALUE);
        return withPermittedTarget(ctx, (src, target) -> {
            PlayerConfigStore.setBoolean(target.getUUID(), key, value);
            PlayerConfigCommandSupport.sendConfigValue(src, key, Boolean.toString(value));
            return 1;
        });
    }
}
