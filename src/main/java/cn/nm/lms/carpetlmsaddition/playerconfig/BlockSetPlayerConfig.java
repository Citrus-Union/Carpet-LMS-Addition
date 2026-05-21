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

import java.util.function.BooleanSupplier;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.lib.BlockRegistryCompat;
import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.playerconfig.command.PlayerConfigCommandSupport;

public final class BlockSetPlayerConfig extends StringSetPlayerConfig {
    private static final String ARG_BLOCK = "block";
    private static final String MESSAGE_UNKNOWN_BLOCK_PREFIX = "Unknown block: ";

    BlockSetPlayerConfig(String key, BooleanSupplier enabled) {
        super(key, ARG_BLOCK, enabled);
    }

    @Override
    protected RequiredArgumentBuilder<CommandSourceStack, ?> addValueArgument(CommandBuildContext commandBuildContext) {
        return Commands.argument(argumentName, ItemArgument.item(commandBuildContext));
    }

    @Nullable
    @Override
    protected String normalizeAddValue(CommandContext<CommandSourceStack> ctx, CommandSourceStack src) {
        Block block = getBlock(ItemArgument.getItem(ctx, argumentName));
        if (block == null) {
            PlayerConfigCommandSupport.sendFailure(src, MESSAGE_UNKNOWN_BLOCK_PREFIX + "not a block item");
            return null;
        }
        return BlockRegistryCompat.getBlockId(block);
    }

    @Nullable
    private static Block getBlock(ItemInput itemInput) {
        Item item = Utils.itemFromInput(itemInput);
        if (item instanceof BlockItem blockItem) {
            return blockItem.getBlock();
        }
        return null;
    }
}
