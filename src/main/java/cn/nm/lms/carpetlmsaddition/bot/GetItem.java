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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import carpet.CarpetServer;
import carpet.patches.EntityPlayerMPFake;

import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.util.storage.Storage;

public class GetItem {
    private static final int MAX_BOT_SCAN_ATTEMPTS = 4096;
    private static final Object GET_ITEM_SERIAL_LOCK = new Object();

    public static Map<String, Map<Item, Integer>> getItem(Item item, int count) {
        if (count <= 0) {
            return Map.of();
        }
        MinecraftServer server = CarpetServer.minecraft_server;
        if (server == null) {
            return Map.of();
        }
        CompletableFuture<Map<String, Map<Item, Integer>>> future = new CompletableFuture<>();
        Thread worker = new Thread(() -> {
            try {
                synchronized (GET_ITEM_SERIAL_LOCK) {
                    future.complete(doGetItem(server, item, count));
                }
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        }, "carpet-lms-getitem-worker");
        worker.setDaemon(true);
        worker.start();
        return future.join();
    }

    private static ItemStack quickMove(AbstractContainerMenu screenHandler, int slotIndex, EntityPlayerMPFake player) {
        return screenHandler.quickMoveStack(player, slotIndex);
    }

    private static FetchResult getItemFromContainer(ServerLevel level, BlockPos pos, EntityPlayerMPFake player,
        Item item, int targetCount) {

        double tpX = pos.getX() + 0.5;
        double tpY = pos.getY() + 0.5 - player.getEyeHeight();
        double tpZ = pos.getZ() + 0.5;

        Utils.teleportTo(player, level, tpX, tpY, tpZ, 0F, 0F);

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof Container container && blockEntity instanceof MenuProvider menuProvider)) {
            return new FetchResult(0, false);
        }

        player.openMenu(menuProvider);

        AbstractContainerMenu screenHandler = player.containerMenu;

        int already = 0;
        boolean inventoryFull = false;
        boolean targetIsShulker = GetItemShulkerUtil.isShulker(item);

        int containerSlotCount = Math.min(container.getContainerSize(), screenHandler.slots.size());
        for (int i = 0; i < containerSlotCount; i++) {
            Slot slot = screenHandler.slots.get(i);
            ItemStack slotStack = slot.getItem();
            int slotContribution = GetItemShulkerUtil.slotAmount(slotStack, item, targetIsShulker);
            if (slotContribution <= 0) {
                continue;
            }

            ItemStack moveRusult = quickMove(screenHandler, i, player);

            if (moveRusult == ItemStack.EMPTY) {
                inventoryFull = true;
                break;
            }

            already += slotContribution;

            if (slot.hasItem()) {
                already -= GetItemShulkerUtil.slotAmount(slot.getItem(), item, targetIsShulker);
                inventoryFull = true;
                break;
            }

            if (already >= targetCount) {
                break;
            }
        }

        player.closeContainer();
        return new FetchResult(already, inventoryFull);
    }

    private static Map<String, Map<Item, Integer>> doGetItem(MinecraftServer server, Item item, int count) {
        int maxBots = Settings.getItemMaxBots;
        int remainingBots = maxBots <= 0 ? Integer.MAX_VALUE : maxBots;
        return doGetItem(server, item, count, 1, 0, remainingBots).result();
    }

    private static RoundResult doGetItem(MinecraftServer server, Item item, int count, int startBotIndex, int depth,
        int remainingBots) {
        if (count <= 0) {
            return new RoundResult(Map.of(), remainingBots);
        }

        List<ContainerTarget> targets = collectTargets(item);
        if (targets.isEmpty()) {
            return new RoundResult(Map.of(), remainingBots);
        }
        targets.sort(Comparator.comparingInt((ContainerTarget target) -> target.pos().getY()).reversed());

        LinkedHashMap<String, Map<Item, Integer>> result = new LinkedHashMap<>();
        int remaining = count;
        int nextBotIndex = startBotIndex;
        int fetchedThisRound = 0;

        EntityPlayerMPFake currentBot = null;
        String currentBotName = null;
        int currentBotFetched = 0;

        try {
            for (ContainerTarget target : targets) {
                if (remaining <= 0) {
                    break;
                }

                if (currentBot == null) {
                    if (remainingBots <= 0) {
                        break;
                    }
                    int botStartIndex = nextBotIndex;
                    SpawnedFake spawned = spawnNextUsableBot(server, target, botStartIndex);
                    currentBot = spawned.player();
                    currentBotName = spawned.name();
                    currentBotFetched = 0;
                    nextBotIndex = spawned.nextIndex();
                    remainingBots--;
                }

                int callTargetCount = remaining;
                EntityPlayerMPFake callBot = currentBot;
                FetchResult fetched = Utils.runOnServerThread(server, () -> {
                    ServerLevel level = server.getLevel(target.dimension());
                    if (level == null) {
                        return new FetchResult(0, false);
                    }
                    return getItemFromContainer(level, target.pos(), callBot, item, callTargetCount);
                });
                int got = fetched.count();
                if (got > 0) {
                    currentBotFetched += got;
                    remaining -= got;
                    fetchedThisRound += got;
                }

                if (remaining > 0) {
                    waitBetweenCalls();
                }

                if (remaining <= 0 || fetched.inventoryFull()) {
                    addOneBotResult(result, currentBotName, item, currentBotFetched);
                    FakePlayerSpawner.silenceLogout(currentBot);
                    currentBot = null;
                    currentBotName = null;
                    currentBotFetched = 0;
                }
            }
        } finally {
            if (currentBot != null) {
                addOneBotResult(result, currentBotName, item, currentBotFetched);
                FakePlayerSpawner.silenceLogout(currentBot);
            }
        }

        if (remaining > 0 && fetchedThisRound > 0 && remainingBots > 0) {
            RoundResult nextRound = doGetItem(server, item, remaining, nextBotIndex, depth + 1, remainingBots);
            remainingBots = nextRound.remainingBots();
            mergeResults(result, nextRound.result(), item);
        }

        return new RoundResult(result, remainingBots);
    }

    private static List<ContainerTarget> collectTargets(Item item) {
        List<ContainerTarget> targets = new ArrayList<>();
        for (Storage.ContainerSnapshot snapshot : Storage.collectConfiguredContainerSnapshots()) {
            if (!hasItem(snapshot, item)) {
                continue;
            }
            Storage.Position position = snapshot.position;
            targets.add(new ContainerTarget(position.dimension, position.pos));
        }
        return targets;
    }

    private static boolean hasItem(Storage.ContainerSnapshot snapshot, Item item) {
        boolean targetIsShulker = GetItemShulkerUtil.isShulker(item);
        for (ItemStack stack : snapshot.stacks) {
            if (GetItemShulkerUtil.slotAmount(stack, item, targetIsShulker) > 0) {
                return true;
            }
        }
        return false;
    }

    private static SpawnedFake spawnNextUsableBot(MinecraftServer server, ContainerTarget target, int startIndex) {
        int index = startIndex;
        int maxIndex = startIndex + MAX_BOT_SCAN_ATTEMPTS;
        while (index < maxIndex) {
            String botName = getBotPrefix() + index;
            index++;

            boolean nameOnline =
                Utils.runOnServerThread(server, () -> server.getPlayerList().getPlayerByName(botName) != null);
            if (nameOnline) {
                continue;
            }
            boolean offlineEmpty =
                Utils.runOnServerThread(server, () -> OfflineInvCheck.isMainInvAndHotbarEmpty(server, botName));
            if (!offlineEmpty) {
                continue;
            }

            Vec3 spawnPos = new Vec3(target.pos().getX() + 0.5, target.pos().getY() + 0.5, target.pos().getZ() + 0.5);
            EntityPlayerMPFake fakePlayer =
                FakePlayerSpawner.spawnSurvivalFakeWithName(server, botName, target.dimension(), spawnPos, true);

            return new SpawnedFake(fakePlayer, botName, index);
        }
        throw new IllegalStateException("No available fake player name in range starting from " + startIndex);
    }

    private static String getBotPrefix() {
        String prefix = Settings.getItemBotPrefix;
        if (prefix == null) {
            return "bot_getitem_";
        }
        String trimmed = prefix.trim();
        return trimmed.isEmpty() ? "bot_getitem_" : trimmed;
    }

    private static void addOneBotResult(Map<String, Map<Item, Integer>> result, String botName, Item item, int count) {
        if (botName == null || count <= 0) {
            return;
        }
        HashMap<Item, Integer> oneBot = new HashMap<>();
        oneBot.put(item, count);
        result.put(botName, oneBot);
    }

    private static void waitBetweenCalls() {
        int delayMillis = Settings.getItemDelayMs;

        if (delayMillis <= 0) {
            return;
        }

        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void mergeResults(Map<String, Map<Item, Integer>> base, Map<String, Map<Item, Integer>> extra,
        Item item) {
        if (extra == null || extra.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Map<Item, Integer>> entry : extra.entrySet()) {
            String botName = entry.getKey();
            int add = entry.getValue().getOrDefault(item, 0);
            if (add <= 0) {
                continue;
            }
            Map<Item, Integer> existing = base.get(botName);
            if (existing == null) {
                HashMap<Item, Integer> oneBot = new HashMap<>();
                oneBot.put(item, add);
                base.put(botName, oneBot);
                continue;
            }
            existing.put(item, existing.getOrDefault(item, 0) + add);
        }
    }

    private record ContainerTarget(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private record FetchResult(int count, boolean inventoryFull) {
    }

    private record SpawnedFake(EntityPlayerMPFake player, String name, int nextIndex) {
    }

    private record RoundResult(Map<String, Map<Item, Integer>> result, int remainingBots) {
    }
}
