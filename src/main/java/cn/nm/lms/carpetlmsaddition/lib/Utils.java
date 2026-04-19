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
package cn.nm.lms.carpetlmsaddition.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public class Utils {
    public static boolean isShulkerBox(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    public static List<ItemStack> nonItemCopyList(ItemContainerContents container) {
        //#if MC>=260100
        return container.nonEmptyItemCopyStream().toList();
        //#else
        //$$ return iterableToList(container.nonEmptyItemsCopy());
        //#endif
    }

    public static <T> List<T> iterableToList(Iterable<T> iterable) {
        if (iterable == null) {
            return Collections.emptyList();
        }

        if (iterable instanceof Collection) {
            return new ArrayList<>((Collection<T>)iterable);
        }

        List<T> list = new ArrayList<>();
        for (T item : iterable) {
            list.add(item);
        }
        return list;
    }

    public static void teleportTo(ServerPlayer player, ServerLevel level, double x, double y, double z, float yaw,
        float pitch) {
        //#if MC >= 12102
        player.teleportTo(level, x, y, z, Set.of(), yaw, pitch, true);
        //#else
        //$$ player.teleportTo(level, x, y, z, yaw, pitch);
        //#endif
    }

    public static <T> T runOnServerThread(MinecraftServer server, Supplier<T> supplier) {
        if (server == null) {
            throw new IllegalStateException("Minecraft server is not initialized");
        }
        if (server.isSameThread()) {
            return supplier.get();
        }
        CompletableFuture<T> future = new CompletableFuture<>();
        server.execute(() -> {
            try {
                future.complete(supplier.get());
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future.join();
    }

    public static Item itemFromInput(ItemInput itemInput) {
        //#if MC>=260100
        return itemInput.item().value();
        //#else
        //$$ return itemInput.getItem();
        //#endif
    }

    public static Component itemDisplayName(Item item) {
        return new ItemStack(item).getHoverName();
    }
}
