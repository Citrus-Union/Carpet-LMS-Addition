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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;
import cn.nm.lms.carpetlmsaddition.lib.ChatEventCompat;
import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.util.command.BaseCommandWithContext;

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
                    CommandSourceStack source = ctx.getSource();
                    ItemInput itemInput = ItemArgument.getItem(ctx, "item");
                    Item item = Utils.itemFromInput(itemInput);
                    int count = IntegerArgumentType.getInteger(ctx, "count");
                    return executeGetItem(source, item, count);
                }))));
    }

    private int executeGetItem(CommandSourceStack source, Item item, int count) {
        int maxCount = Settings.getItemMaxCount;
        if (count < 1) {
            source.sendFailure(Component.literal("Count must be at least 1"));
            return 0;
        }
        if (maxCount > 0 && count > maxCount) {
            source.sendFailure(Component.literal(String.format("Count must be between 1 and %d", maxCount)));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("getItem started in background"), false);
        try {
            AsyncTasks.run(() -> runGetItemAsync(source, item, count));
            return 0;
        } catch (Throwable throwable) {
            source.sendFailure(Component.literal(buildFailureMessage(throwable)));
            return 1;
        }
    }

    private void sendResult(CommandSourceStack source, Item item, Map<String, Map<Item, Integer>> result) {
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
            source.sendSuccess(() -> buildBotResultLine(botName, itemName, got), false);
        }
        int totalResult = total;
        source.sendSuccess(
            () -> Component.literal("getItem done: ").append(itemName).append(Component.literal(" x" + totalResult)),
            false);
    }

    private void runGetItemAsync(CommandSourceStack source, Item item, int count) {
        try {
            Map<String, Map<Item, Integer>> result = GetItem.getItem(item, count);
            source.getServer().execute(() -> sendResult(source, item, result));
        } catch (Throwable throwable) {
            source.getServer().execute(() -> source.sendFailure(Component.literal(buildFailureMessage(throwable))));
        }
    }

    private String buildFailureMessage(Throwable throwable) {
        String msg = throwable.getMessage();
        return "getItem failed: " + (msg == null ? "unknown" : msg);
    }

    private Component buildBotResultLine(String botName, Component itemName, int got) {
        String spawnCommand = "/player " + botName + " spawn";
        String killCommand = "/player " + botName + " kill";
        String inventoryCommand = "/player " + botName + " inventory";

        Component up =
            Component.literal("[↑]").withStyle(style -> style.withClickEvent(ChatEventCompat.runCommand(spawnCommand))
                .withHoverEvent(ChatEventCompat.showText(Component.literal("spawn"))));
        Component down =
            Component.literal("[↓]").withStyle(style -> style.withClickEvent(ChatEventCompat.runCommand(killCommand))
                .withHoverEvent(ChatEventCompat.showText(Component.literal("kill"))));
        Component openInventory = Component.literal("[O]")
            .withStyle(style -> style.withClickEvent(ChatEventCompat.runCommand(inventoryCommand))
                .withHoverEvent(ChatEventCompat.showText(Component.literal("inventory"))));

        return Component.literal(botName).append(up).append(down).append(openInventory).append(Component.literal(": "))
            .append(itemName.copy()).append(Component.literal(" x" + got));
    }
}
