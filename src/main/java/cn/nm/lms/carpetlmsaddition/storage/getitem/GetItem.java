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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import carpet.patches.EntityPlayerMPFake;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.bot.FakePlayerSpawner;
import cn.nm.lms.carpetlmsaddition.lib.ChatEventCompat;
import cn.nm.lms.carpetlmsaddition.lib.NameRateLimiter;
import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.storage.StorageSlotCounter;
import cn.nm.lms.carpetlmsaddition.storage.StorageSlotCounter.Result;
import cn.nm.lms.carpetlmsaddition.storage.data.Storage;
import cn.nm.lms.carpetlmsaddition.storage.data.StorageContainerReader.SlotSnapshot;
import cn.nm.lms.carpetlmsaddition.storage.getitem.GetItemSlotSelector.Target;

public class GetItem {
    private static final NameRateLimiter RATE_LIMITER = new NameRateLimiter();

    public static synchronized Map<String, Map<Item, Integer>> getItem(Item item, int count,
        @Nullable String playerName) {
        return getItemWithStats(item, count, playerName).result();
    }

    public static synchronized GetItemResult getItemWithStats(Item item, int count, @Nullable String playerName) {
        checkRateLimit(playerName);
        Item target = StorageSlotCounter.normalize(item);
        return doGetItem(target, count);
    }

    public static Component buildBotResultLine(String botName, Item item, int got) {
        String spawnCommand = "/player " + botName + " spawn";
        String killCommand = "/player " + botName + " kill";
        String inventoryCommand = "/player " + botName + " inventory";
        Component itemName = Utils.itemDisplayName(item);

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

    private static void checkRateLimit(@Nullable String playerName) {
        RATE_LIMITER.check("getItem", playerName, Settings.getItemCooldownSeconds);
    }

    private static ItemStack quickMove(AbstractContainerMenu screenHandler, int slotIndex, EntityPlayerMPFake player) {
        return screenHandler.quickMoveStack(player, slotIndex);
    }

    private static FetchResult getItemFromSlot(ServerLevel level, BlockPos pos, int slotIndex,
        EntityPlayerMPFake player, Item item) {

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

        int containerSlotCount = Math.min(container.getContainerSize(), screenHandler.slots.size());
        if (slotIndex < 0 || slotIndex >= containerSlotCount) {
            player.closeContainer();
            return new FetchResult(0, false);
        }

        Slot slot = screenHandler.slots.get(slotIndex);
        int slotContribution = countTarget(slot.getItem(), item);
        if (slotContribution <= 0) {
            player.closeContainer();
            return new FetchResult(0, false);
        }

        ItemStack moveResult = quickMove(screenHandler, slotIndex, player);

        if (moveResult == ItemStack.EMPTY) {
            player.closeContainer();
            return new FetchResult(0, true);
        }

        int got = slotContribution;
        boolean inventoryFull = false;
        if (slot.hasItem()) {
            got -= countTarget(slot.getItem(), item);
            inventoryFull = true;
        }

        player.closeContainer();
        return new FetchResult(got, inventoryFull);
    }

    private static GetItemResult doGetItem(Item item, int count) {
        int maxBots = Settings.getItemMaxBots;
        int remainingBots = maxBots <= 0 ? Integer.MAX_VALUE : maxBots;
        RoundResult roundResult = doGetItem(item, count, 1, remainingBots);
        return new GetItemResult(roundResult.result());
    }

    private static RoundResult doGetItem(Item item, int count, int startBotIndex, int remainingBots) {
        if (count <= 0) {
            return new RoundResult(Map.of(), remainingBots);
        }

        List<Target> allTargets = collectSlotTargets(item);
        List<Target> targets = GetItemSlotSelector.select(allTargets, count);
        if (targets.isEmpty()) {
            return new RoundResult(Map.of(), remainingBots);
        }
        targets.sort(Comparator.comparingInt((Target target) -> target.pos().getY()).reversed());

        LinkedHashMap<String, Map<Item, Integer>> result = new LinkedHashMap<>();
        int remaining = count;
        int nextBotIndex = startBotIndex;
        int fetchedThisRound = 0;

        EntityPlayerMPFake currentBot = null;
        String currentBotName = null;
        int currentBotFetched = 0;

        try {
            for (Target target : targets) {
                if (remaining <= 0) {
                    break;
                }

                if (currentBot == null) {
                    if (remainingBots <= 0) {
                        break;
                    }
                    int botStartIndex = nextBotIndex;
                    SpawnedFake spawned = spawnNextUsableBot(target, botStartIndex);
                    currentBot = spawned.player();
                    currentBotName = spawned.name();
                    currentBotFetched = 0;
                    nextBotIndex = spawned.nextIndex();
                    remainingBots--;
                }

                EntityPlayerMPFake callBot = currentBot;
                FetchResult fetched = Utils.runOnServerThread(() -> {
                    MinecraftServer server = Utils.getServer();
                    ServerLevel level = server.getLevel(target.dimension());
                    if (level == null) {
                        return new FetchResult(0, false);
                    }
                    return getItemFromSlot(level, target.pos(), target.slotIndex(), callBot, item);
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
            RoundResult nextRound = doGetItem(item, remaining, nextBotIndex, remainingBots);
            remainingBots = nextRound.remainingBots();
            mergeResults(result, nextRound.result(), item);
        }

        return new RoundResult(result, remainingBots);
    }

    private static List<Target> collectSlotTargets(Item item) {
        List<Target> targets = new ArrayList<>();
        for (SlotSnapshot snapshot : Storage.collectAllConfiguredStorageSnapshots().snapshots()) {
            Storage.Position position = snapshot.position();
            Result result = StorageSlotCounter.count(snapshot.stack());
            if (result == null || !result.matches(item)) {
                continue;
            }
            targets.add(new Target(position.dimension, position.pos, snapshot.slotIndex(), result.count(),
                result.noShulkerBox()));
        }
        targets.sort(Comparator.comparingInt((Target target) -> target.pos().getY()).reversed());
        return targets;
    }

    private static int countTarget(ItemStack stack, Item target) {
        Result result = StorageSlotCounter.count(stack);
        if (result == null || !result.matches(target)) {
            return 0;
        }
        return result.count();
    }

    private static SpawnedFake spawnNextUsableBot(Target target, int startIndex) {
        int index = startIndex;
        int maxIndex = GetItemBotHelper.BOT_SCAN_LIMIT;
        while (index < maxIndex) {
            String botName = GetItemBotHelper.getBotPrefix() + index;
            index++;

            boolean nameOnline = GetItemBotHelper.isBotOnline(botName);
            if (nameOnline) {
                continue;
            }
            boolean offlineEmpty = OfflineInvCheck.isInventoryEmpty(botName);
            if (!offlineEmpty) {
                continue;
            }

            Vec3 spawnPos = new Vec3(target.pos().getX() + 0.5, target.pos().getY() + 0.5, target.pos().getZ() + 0.5);
            EntityPlayerMPFake fakePlayer =
                FakePlayerSpawner.spawnSurvivalFakeWithName(botName, target.dimension(), spawnPos, true, true);

            return new SpawnedFake(fakePlayer, botName, index);
        }
        throw new IllegalStateException("No available fake player name in range starting from " + startIndex);
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

    private record FetchResult(int count, boolean inventoryFull) {
    }

    private record SpawnedFake(EntityPlayerMPFake player, String name, int nextIndex) {
    }

    public record GetItemResult(Map<String, Map<Item, Integer>> result) {
    }

    private record RoundResult(Map<String, Map<Item, Integer>> result, int remainingBots) {
    }
}
