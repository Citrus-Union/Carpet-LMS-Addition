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
package cn.nm.lms.carpetlmsaddition.rule.util.storage;

import java.util.Comparator;
import java.util.Map;

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

import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.util.command.BaseCommandWithContext;

public final class CommandGetStorageData implements BaseCommandWithContext {
    private static boolean canUseGetStorageDataCommand(CommandSourceStack source) {
        return CommandHelper.canUseCommand(source, Settings.commandGetStorageData);
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

    private static int executeGetAll(CommandSourceStack source) {
        Map<Item, Integer> counts = Storage.generateStorageItemCounts();
        ListTag list = new ListTag();

        counts.entrySet().stream()
            .sorted(Comparator.comparing(entry -> BuiltInRegistries.ITEM.getKey(entry.getKey()).toString()))
            .forEach(entry -> list.add(toResultTag(entry.getKey(), entry.getValue())));

        Component component = new TextComponentTagVisitor("").visit(list);
        source.sendSuccess(() -> component, false);
        return 1;
    }

    private static int executeGetOne(CommandContext<CommandSourceStack> ctx) {
        Item item = Utils.itemFromInput(ItemArgument.getItem(ctx, "id"));
        int count = Storage.generateStorageItemCounts().getOrDefault(item, 0);
        CompoundTag tag = toResultTag(item, count);

        Component component = new TextComponentTagVisitor("").visit(tag);
        ctx.getSource().sendSuccess(() -> component, false);
        return 1;
    }

    private static CompoundTag toResultTag(Item item, int count) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", BuiltInRegistries.ITEM.getKey(item).toString());
        tag.putInt("count", count);
        return tag;
    }
}
