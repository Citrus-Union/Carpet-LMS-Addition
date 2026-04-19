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

import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import carpet.patches.EntityPlayerMPFake;

import cn.nm.lms.carpetlmsaddition.lib.Utils;

public final class FakePlayerSpawner {
    private static final long SPAWN_POLL_INTERVAL_MILLIS = 50L;
    private static final int SPAWN_TIMEOUT_TICKS = 120;
    private static final ThreadLocal<Integer> SILENCE_DEPTH = ThreadLocal.withInitial(() -> 0);

    private FakePlayerSpawner() {}

    public static EntityPlayerMPFake spawnSurvivalFakeWithName(MinecraftServer server, String botName,
        ResourceKey<Level> dimension, Vec3 spawnPos) {
        return spawnSurvivalFakeWithName(server, botName, dimension, spawnPos, false);
    }

    public static EntityPlayerMPFake spawnSurvivalFakeWithName(MinecraftServer server, String botName,
        ResourceKey<Level> dimension, Vec3 spawnPos, boolean silence) {
        boolean nameOnline =
            Utils.runOnServerThread(server, () -> server.getPlayerList().getPlayerByName(botName) != null);
        if (nameOnline) {
            throw new IllegalStateException("Fake player already online: " + botName);
        }

        boolean created = Utils.runOnServerThread(server, () -> callWithSilence(silence, () -> EntityPlayerMPFake
            .createFake(botName, server, spawnPos, 0.0, 0.0, dimension, GameType.SURVIVAL, false)));
        if (!created) {
            throw new IllegalStateException("Unable to spawn fake player: " + botName);
        }

        EntityPlayerMPFake fakePlayer = awaitFakePlayerOnline(server, botName);
        if (fakePlayer == null) {
            throw new IllegalStateException("Timed out waiting for fake player online: " + botName);
        }
        return fakePlayer;
    }

    private static EntityPlayerMPFake awaitFakePlayerOnline(MinecraftServer server, String botName) {
        for (int tick = 0; tick < SPAWN_TIMEOUT_TICKS; tick++) {
            EntityPlayerMPFake fakePlayer = Utils.runOnServerThread(server, () -> {
                if (server.getPlayerList().getPlayerByName(botName) instanceof EntityPlayerMPFake player) {
                    return player;
                }
                return null;
            });
            if (fakePlayer != null) {
                return fakePlayer;
            }
            waitSpawnPoll();
        }
        return Utils.runOnServerThread(server, () -> {
            if (server.getPlayerList().getPlayerByName(botName) instanceof EntityPlayerMPFake player) {
                return player;
            }
            return null;
        });
    }

    public static void logout(EntityPlayerMPFake fakePlayer) {
        fakePlayer.kill(Component.literal("fake player logout"));
    }

    public static void silenceLogout(EntityPlayerMPFake fakePlayer) {
        MinecraftServer server = fakePlayer.level().getServer();
        Utils.runOnServerThread(server, () -> callWithSilence(true, () -> {
            logout(fakePlayer);
            return null;
        }));
    }

    public static boolean isSilenceEnabled() {
        return SILENCE_DEPTH.get() > 0;
    }

    public static void runWithSilenceScope(boolean silence, Runnable runnable) {
        callWithSilence(silence, () -> {
            runnable.run();
            return null;
        });
    }

    private static void waitSpawnPoll() {
        try {
            Thread.sleep(SPAWN_POLL_INTERVAL_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static <T> T callWithSilence(boolean silence, Supplier<T> supplier) {
        if (!silence) {
            return supplier.get();
        }
        SILENCE_DEPTH.set(SILENCE_DEPTH.get() + 1);
        try {
            return supplier.get();
        } finally {
            int depth = SILENCE_DEPTH.get() - 1;
            if (depth <= 0) {
                SILENCE_DEPTH.remove();
            } else {
                SILENCE_DEPTH.set(depth);
            }
        }
    }
}
