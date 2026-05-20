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
package cn.nm.lms.carpetlmsaddition.storage.data;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;
import cn.nm.lms.carpetlmsaddition.lib.NameRateLimiter;
import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.util.command.BaseCommandWithContext;
import cn.nm.lms.carpetlmsaddition.rule.util.command.CommandRateLimitNbt;
import cn.nm.lms.carpetlmsaddition.storage.StorageSlotCounter;

public final class CommandGetStorageData implements BaseCommandWithContext {
    private static boolean canUseGetStorageDataCommand(CommandSourceStack source) {
        return CommandHelper.canUseCommand(source, Settings.commandGetStorageData);
    }

    private static int executeGetAll(CommandSourceStack source) {
        if (!checkRateLimit(source)) {
            return 0;
        }
        sendCountsAsync(source, counts -> {
            ListTag list = new ListTag();
            counts.entrySet().stream()
                .sorted(Comparator.comparing(entry -> BuiltInRegistries.ITEM.getKey(entry.getKey()).toString()))
                .forEach(entry -> list.add(toResultTag(entry.getKey(), entry.getValue())));
            return new TextComponentTagVisitor("").visit(list);
        });
        return 1;
    }

    private static int executeGetOne(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!checkRateLimit(source)) {
            return 0;
        }
        Item item = StorageSlotCounter.normalize(Utils.itemFromInput(ItemArgument.getItem(ctx, "id")));
        sendCountsAsync(source, counts -> {
            int count = counts.getOrDefault(item, 0);
            CompoundTag tag = toResultTag(item, count);
            return new TextComponentTagVisitor("").visit(tag);
        });
        return 1;
    }

    private static void sendCountsAsync(CommandSourceStack source, Function<Map<Item, Integer>, Component> formatter) {
        generateStorageItemCountsAsync().thenApply(formatter).thenAccept(component -> {
            source.getServer().execute(() -> source.sendSuccess(() -> component, false));
        }).exceptionally(e -> {
            source.getServer().execute(() -> source.sendFailure(Component.literal(getErrorMessage(e))));
            return null;
        });
    }

    private static String getErrorMessage(Throwable throwable) {
        Throwable cause = throwable.getCause() == null ? throwable : throwable.getCause();
        return cause.getMessage() == null ? cause.toString() : cause.getMessage();
    }

    private static boolean checkRateLimit(CommandSourceStack source) {
        try {
            Storage.checkGetDataRateLimit(source.getTextName());
            return true;
        } catch (NameRateLimiter.RateLimitException e) {
            CommandRateLimitNbt.sendWaitSecond(source, e.waitSeconds());
            return false;
        }
    }

    private static CompoundTag toResultTag(Item item, int count) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", BuiltInRegistries.ITEM.getKey(item).toString());
        tag.putInt("count", count);
        return tag;
    }

    private static CompletableFuture<Map<Item, Integer>> generateStorageItemCountsAsync() {
        return AsyncTasks.supply(Storage::generateStorageItemCounts);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher,
        final CommandBuildContext commandBuildContext) {
        dispatcher.register(Commands.literal("getStorageData")
            .requires(CommandGetStorageData::canUseGetStorageDataCommand).executes(ctx -> {
                return executeGetAll(ctx.getSource());
            }).then(Commands.argument("id", ItemArgument.item(commandBuildContext)).executes(ctx -> {
                return executeGetOne(ctx);
            })));
    }
}
