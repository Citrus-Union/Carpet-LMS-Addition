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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.playerconfig.command.PlayerConfigCommandSupport;

public abstract class PlayerConfigEntry<T> {
    protected final String key;
    private final BooleanSupplier enabled;

    PlayerConfigEntry(String key, BooleanSupplier enabled) {
        this.key = key;
        this.enabled = enabled;
    }

    public boolean enabled() {
        return enabled.getAsBoolean();
    }

    public String key() {
        return key;
    }

    public abstract @Nullable T get(UUID playerUUID);

    public final @Nullable T get(Player player) {
        return get(player.getUUID());
    }

    public abstract ArgumentBuilder<CommandSourceStack, ?> build(CommandBuildContext commandBuildContext);

    protected final int executeGet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withPermittedTarget(ctx, (src, target) -> {
            PlayerConfigCommandSupport.sendConfigValue(src, key, formatValue(get(target.getUUID())));
            return 1;
        });
    }

    protected final int withPermittedTarget(CommandContext<CommandSourceStack> ctx, TargetAction action)
        throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer target = PlayerConfigCommandSupport.getTarget(ctx);
        if (!PlayerConfigCommandSupport.hasPermission(src, target)) {
            return 0;
        }
        return action.run(src, target);
    }

    protected String formatValue(@Nullable T value) {
        return value == null ? "null" : value.toString();
    }

    @FunctionalInterface
    protected interface TargetAction {
        int run(CommandSourceStack src, ServerPlayer target) throws CommandSyntaxException;
    }
}
