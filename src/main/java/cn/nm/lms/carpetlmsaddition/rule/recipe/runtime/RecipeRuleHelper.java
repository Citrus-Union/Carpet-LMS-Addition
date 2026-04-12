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
package cn.nm.lms.carpetlmsaddition.rule.recipe.runtime;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;

import carpet.CarpetServer;

import cn.nm.lms.carpetlmsaddition.Mod;

public final class RecipeRuleHelper {
    private static boolean startupReloadPending;

    public static void onValueChange() {
        LmsRecipeManager.clearRecipes();
        LmsRecipeManager.rebuildRecipes();
        if (!reloadRecipes()) {
            startupReloadPending = true;
        }
    }

    public static Collection<RecipeHolder<?>> getRecipes() {
        return LmsRecipeManager.getCustomRecipes();
    }

    public static Collection<RecipeHolder<?>> mergeWithManagedRecipes(Iterable<RecipeHolder<?>> existingRecipes) {
        Map<Object, RecipeHolder<?>> merged = new LinkedHashMap<>();
        for (RecipeHolder<?> recipeHolder : existingRecipes) {
            merged.put(recipeHolder.id(), recipeHolder);
        }
        for (RecipeHolder<?> recipeHolder : getRecipes()) {
            merged.put(recipeHolder.id(), recipeHolder);
        }
        return merged.values();
    }

    public static void flushPendingReload(MinecraftServer server) {
        if (!startupReloadPending) {
            return;
        }
        if (reloadRecipes(server)) {
            startupReloadPending = false;
        }
    }

    private static boolean reloadRecipes() {
        return reloadRecipes(CarpetServer.minecraft_server);
    }

    private static boolean reloadRecipes(MinecraftServer server) {
        // carpet.conf loading may happen before Carpet script server is initialized.
        // Triggering reloadResources too early would crash in Carpet command re-registering.
        if (server == null || CarpetServer.scriptServer == null) {
            return false;
        }
        server.execute(() -> server.reloadResources(server.getPackRepository().getSelectedIds())
            .thenRun(() -> server.execute(() -> RecipeBookHelper.syncOnlinePlayers(server)))
            .exceptionally(throwable -> {
                Mod.LOGGER.error("Failed to reload resources after recipe rule changed", throwable);
                return null;
            }));
        return true;
    }
}
