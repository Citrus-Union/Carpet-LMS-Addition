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
package cn.nm.lms.carpetlmsaddition.rule.compat.tis.largebarrel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.loader.api.FabricLoader;

import cn.nm.lms.carpetlmsaddition.Mod;

/**
 * Runtime bridge to Carpet TIS Addition's large barrel helpers without a compile-time mod dependency.
 */
public final class TisLargeBarrelCompat {
    private static final boolean TIS_LOADED = FabricLoader.getInstance().isModLoaded("carpet-tis-addition");

    @Nullable
    private static final Field LARGE_BARREL_RULE;

    @Nullable
    private static final Method GET_INVENTORY;

    @Nullable
    private static final Class<?> REMOVABLE_BLOCK_ENTITY;

    @Nullable
    private static final Method INCREASE_REMOVE_COUNTER;

    static {
        Field largeBarrelRule = null;
        Method getInventory = null;
        Class<?> removableBlockEntity = null;
        Method increaseRemoveCounter = null;

        if (TIS_LOADED) {
            try {
                Class<?> settings = Class.forName("carpettisaddition.CarpetTISAdditionSettings");
                largeBarrelRule = settings.getField("largeBarrel");
                Class<?> helper = Class.forName("carpettisaddition.helpers.rule.largeBarrel.LargeBarrelHelper");
                getInventory = helper.getMethod("getInventory", BlockState.class, Level.class, BlockPos.class);
            } catch (ReflectiveOperationException exception) {
                Mod.LOGGER.error("Failed to bind Carpet TIS Addition large barrel helpers", exception);
            }

            for (String className : new String[] {
                "me.jellysquid.mods.lithium.common.hopper.RemovableBlockEntity",
                "net.caffeinemc.mods.lithium.common.hopper.RemovableBlockEntity",
            }) {
                try {
                    removableBlockEntity = Class.forName(className);
                    increaseRemoveCounter = removableBlockEntity.getMethod("increaseRemoveCounter");
                    break;
                } catch (ReflectiveOperationException ignored) {
                    // try next package
                }
            }
        }

        LARGE_BARREL_RULE = largeBarrelRule;
        GET_INVENTORY = getInventory;
        REMOVABLE_BLOCK_ENTITY = removableBlockEntity;
        INCREASE_REMOVE_COUNTER = increaseRemoveCounter;
    }

    private TisLargeBarrelCompat() {}

    public static boolean isAvailable() {
        return TIS_LOADED && LARGE_BARREL_RULE != null && GET_INVENTORY != null;
    }

    public static boolean isTisLargeBarrelEnabled() {
        if (LARGE_BARREL_RULE == null) {
            return false;
        }
        try {
            return LARGE_BARREL_RULE.getBoolean(null);
        } catch (IllegalAccessException exception) {
            Mod.LOGGER.error("Failed to read Carpet TIS Addition largeBarrel rule", exception);
            return false;
        }
    }

    @Nullable
    public static Container getLargeBarrelInventory(BlockState state, Level world, BlockPos pos) {
        if (GET_INVENTORY == null) {
            return null;
        }
        try {
            return (Container)GET_INVENTORY.invoke(null, state, world, pos);
        } catch (ReflectiveOperationException exception) {
            Mod.LOGGER.error("Failed to query large barrel inventory at {}", pos, exception);
            return null;
        }
    }

    public static void invalidateLithiumHopperCache(BlockEntity blockEntity) {
        if (REMOVABLE_BLOCK_ENTITY == null || INCREASE_REMOVE_COUNTER == null) {
            return;
        }
        if (!REMOVABLE_BLOCK_ENTITY.isInstance(blockEntity)) {
            return;
        }
        try {
            INCREASE_REMOVE_COUNTER.invoke(blockEntity);
        } catch (ReflectiveOperationException exception) {
            Mod.LOGGER.error("Failed to invalidate lithium hopper cache for {}", blockEntity, exception);
        }
    }
}
