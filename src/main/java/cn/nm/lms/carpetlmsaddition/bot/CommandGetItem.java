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
package cn.nm.lms.carpetlmsaddition.bot;

import java.util.Map;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;
import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.util.command.BaseCommandWithContext;
import cn.nm.lms.carpetlmsaddition.rule.util.command.CommandRateLimitNbt;

public final class CommandGetItem implements BaseCommandWithContext {
    private static boolean canUseGetItemCommand(CommandSourceStack source) {
        return CommandHelper.canUseCommand(source, Settings.commandGetItem);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher,
        final CommandBuildContext commandBuildContext) {
        dispatcher.register(Commands.literal("getItem").requires(CommandGetItem::canUseGetItemCommand)
            .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                .then(Commands.argument("count", IntegerArgumentType.integer(1)).executes(ctx -> {
                    return executeGetItem(ctx, false);
                }).then(Commands.argument("nbt", StringArgumentType.word()).suggests((ctx, builder) -> {
                    builder.suggest("nbt");
                    return builder.buildFuture();
                }).executes(ctx -> {
                    return executeGetItem(ctx, true);
                })))));
    }

    private int executeGetItem(CommandContext<CommandSourceStack> ctx, boolean nbt) {
        CommandSourceStack source = ctx.getSource();
        ItemInput itemInput = ItemArgument.getItem(ctx, "item");
        Item item = Utils.itemFromInput(itemInput);
        int count = IntegerArgumentType.getInteger(ctx, "count");
        String playerName = source.getTextName();
        int maxCount = Settings.getItemMaxCount;
        if (count < 1) {
            if (nbt) {
                return getItemFailNbt(source, maxCount);
            }
            source.sendFailure(Component.literal("Count must be at least 1"));
            return 0;
        }
        if (maxCount > 0 && count > maxCount) {
            if (nbt) {
                return getItemFailNbt(source, maxCount);
            }
            source.sendFailure(Component.literal(String.format("Count must be between 1 and %d", maxCount)));
            return 0;
        }
        if (!nbt) {
            source.sendSuccess(() -> Component.literal("getItem started in background"), false);
        }
        try {
            AsyncTasks.run(() -> runGetItemAsync(source, item, count, playerName, nbt));
            return 0;
        } catch (Throwable throwable) {
            if (nbt && CommandRateLimitNbt.sendIfRateLimited(source, throwable)) {
                return 0;
            }
            source.sendFailure(Component.literal(buildFailureMessage(throwable)));
            return 1;
        }
    }

    private int getItemFailNbt(CommandSourceStack source, int maxCount) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("maxCount", maxCount);
        Component component = new TextComponentTagVisitor("").visit(tag);

        source.sendFailure(component);
        return 0;
    }

    private void sendResultText(CommandSourceStack source, Item item, Map<String, Map<Item, Integer>> result) {
        Component itemName = Utils.itemDisplayName(item);
        if (result.isEmpty()) {
            source.sendSuccess(
                () -> Component.literal("getItem done: ").append(itemName).append(Component.literal(" x0")), false);
            return;
        }

        int total = 0;
        for (Map.Entry<String, Map<Item, Integer>> entry : result.entrySet()) {
            int got = entry.getValue().getOrDefault(item, 0);
            total += got;
            String botName = entry.getKey();
            source.sendSuccess(() -> GetItem.buildBotResultLine(botName, item, got), false);
        }
        int totalResult = total;
        source.sendSuccess(
            () -> Component.literal("getItem done: ").append(itemName).append(Component.literal(" x" + totalResult)),
            false);
    }

    private void sendResultNbt(CommandSourceStack source, Map<String, Map<Item, Integer>> result) {
        ListTag list = new ListTag();

        for (Map.Entry<String, Map<Item, Integer>> outer : result.entrySet()) {
            String name = outer.getKey();
            for (Map.Entry<Item, Integer> inner : outer.getValue().entrySet()) {
                Item item = inner.getKey();
                int count = inner.getValue();

                CompoundTag tag = new CompoundTag();

                tag.putString("name", name);
                tag.putString("id", BuiltInRegistries.ITEM.getKey(item).toString());
                tag.putInt("count", count);

                list.add(tag);
            }
        }

        Component component = new TextComponentTagVisitor("").visit(list);

        source.sendSuccess(() -> component, false);
    }

    private void sendResult(CommandSourceStack source, Item item, Map<String, Map<Item, Integer>> result, boolean nbt) {
        if (nbt) {
            sendResultNbt(source, result);
        } else {
            sendResultText(source, item, result);
        }
    }

    private void runGetItemAsync(CommandSourceStack source, Item item, int count, String playerName, boolean nbt) {
        try {
            Map<String, Map<Item, Integer>> result = GetItem.getItem(item, count, playerName);
            source.getServer().execute(() -> sendResult(source, item, result, nbt));
        } catch (Throwable throwable) {
            source.getServer().execute(() -> {
                if (nbt && CommandRateLimitNbt.sendIfRateLimited(source, throwable)) {
                    return;
                }
                source.sendFailure(Component.literal(buildFailureMessage(throwable)));
            });
        }
    }

    private String buildFailureMessage(Throwable throwable) {
        String msg = throwable.getMessage();
        return "getItem failed: " + (msg == null ? "unknown" : msg);
    }
}
