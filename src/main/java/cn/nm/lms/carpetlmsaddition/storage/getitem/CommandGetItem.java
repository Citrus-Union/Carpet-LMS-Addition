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
package cn.nm.lms.carpetlmsaddition.storage.getitem;

import java.util.Map;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import cn.nm.lms.carpetlmsaddition.Mod;
import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;
import cn.nm.lms.carpetlmsaddition.lib.MessageComponent;
import cn.nm.lms.carpetlmsaddition.lib.NameRateLimiter;
import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.util.command.BaseCommandWithContext;
import cn.nm.lms.carpetlmsaddition.rule.util.command.CommandRateLimitNbt;
import cn.nm.lms.carpetlmsaddition.rule.util.command.CommandUtils;
import cn.nm.lms.carpetlmsaddition.storage.StorageSlotCounter;
import cn.nm.lms.carpetlmsaddition.translations.Translations;

public final class CommandGetItem implements BaseCommandWithContext {
    private enum OutputMode {
        TEXT, NBT
    }

    private static boolean checkPermission(CommandSourceStack source) {
        return CommandUtils.hasPermission(source, Settings.commandGetItem, "/getItem", "commandGetItem");
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher,
        final CommandBuildContext commandBuildContext) {
        dispatcher.register(Commands.literal("getItem").executes(ctx -> tutor(ctx.getSource()))
            .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                .then(Commands.argument("count", IntegerArgumentType.integer(1)).executes(ctx -> {
                    return executeGetItem(ctx, OutputMode.TEXT);
                }).then(Commands.argument("mode", StringArgumentType.word()).suggests((ctx, builder) -> {
                    builder.suggest("nbt");
                    return builder.buildFuture();
                }).executes(ctx -> {
                    return executeGetItem(ctx, parseMode(StringArgumentType.getString(ctx, "mode")));
                })))));
    }

    private int tutor(CommandSourceStack source) {
        if (!checkPermission(source)) {
            return 0;
        }
        CommandUtils.tutor(source, "getItem", 4);
        return 1;
    }

    private static OutputMode parseMode(String mode) {
        if ("nbt".equals(mode)) {
            return OutputMode.NBT;
        }
        return OutputMode.TEXT;
    }

    private int executeGetItem(CommandContext<CommandSourceStack> ctx, OutputMode mode) {
        CommandSourceStack source = ctx.getSource();
        if (!checkPermission(source)) {
            return 0;
        }
        ItemInput itemInput = ItemArgument.getItem(ctx, "item");
        Item item = StorageSlotCounter.normalize(Utils.itemFromInput(itemInput));
        int count = IntegerArgumentType.getInteger(ctx, "count");
        String playerName = source.getTextName();
        int maxCount = Settings.getItemMaxCount;
        if (count < 1) {
            if (mode == OutputMode.NBT) {
                return getItemFailNbt(source, maxCount);
            }
            new MessageComponent("getItem.countTooSmall").sendFailure(source);
            return 0;
        }
        if (maxCount > 0 && count > maxCount) {
            if (mode == OutputMode.NBT) {
                return getItemFailNbt(source, maxCount);
            }
            new MessageComponent("getItem.countOutOfRange", maxCount).sendFailure(source);
            return 0;
        }
        if (mode != OutputMode.NBT) {
            new MessageComponent("getItem.started").sendSuccess(source);
        }
        try {
            AsyncTasks.run(() -> runGetItemAsync(source, item, count, playerName, mode));
            return 0;
        } catch (Throwable throwable) {
            if (mode == OutputMode.NBT && CommandRateLimitNbt.sendIfRateLimited(source, throwable)) {
                return 0;
            }
            Mod.LOGGER.warn("getItem failed to start", throwable);
            Translations.FeedbackMessage message = buildFailureMessage(throwable);
            new MessageComponent(message.key(), message.args()).sendFailure(source);
            return 1;
        }
    }

    private int getItemFailNbt(CommandSourceStack source, int maxCount) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("maxCount", maxCount);
        new MessageComponent(tag).sendFailure(source);
        return 0;
    }

    private void sendResultText(CommandSourceStack source, Item item, Map<String, Map<Item, Integer>> result) {
        Component itemName = Utils.itemDisplayName(item);
        if (result.isEmpty()) {
            new MessageComponent("getItem.donePrefix").append(itemName)
                .append(Translations.messageTr("getItem.doneCount", 0)).sendSuccess(source);
            return;
        }

        int total = 0;
        for (Map.Entry<String, Map<Item, Integer>> entry : result.entrySet()) {
            int got = entry.getValue().getOrDefault(item, 0);
            total += got;
            String botName = entry.getKey();
            GetItem.buildBotResultLine(botName, item, got).sendSuccess(source);
        }
        int totalResult = total;
        new MessageComponent("getItem.donePrefix").append(itemName)
            .append(Translations.messageTr("getItem.doneCount", totalResult)).sendSuccess(source);
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

        new MessageComponent(list).sendSuccess(source);
    }

    private void sendResult(CommandSourceStack source, Item item, GetItem.GetItemResult result, OutputMode mode) {
        if (mode == OutputMode.NBT) {
            sendResultNbt(source, result.result());
        } else {
            sendResultText(source, item, result.result());
        }
    }

    private void runGetItemAsync(CommandSourceStack source, Item item, int count, String playerName, OutputMode mode) {
        try {
            GetItem.GetItemResult result = GetItem.getItemWithStats(item, count, playerName);
            source.getServer().execute(() -> sendResult(source, item, result, mode));
        } catch (Throwable throwable) {
            source.getServer().execute(() -> {
                if (mode == OutputMode.NBT && CommandRateLimitNbt.sendIfRateLimited(source, throwable)) {
                    return;
                }
                Mod.LOGGER.warn("getItem failed in background", throwable);
                Translations.FeedbackMessage message = buildFailureMessage(throwable);
                new MessageComponent(message.key(), message.args()).sendFailure(source);
            });
        }
    }

    private Translations.FeedbackMessage buildFailureMessage(Throwable throwable) {
        if (throwable instanceof NameRateLimiter.RateLimitException rateLimitException) {
            return new Translations.FeedbackMessage("getItem.rateLimited", rateLimitException.waitSeconds());
        }
        return new Translations.FeedbackMessage("common.unknownError");
    }
}
