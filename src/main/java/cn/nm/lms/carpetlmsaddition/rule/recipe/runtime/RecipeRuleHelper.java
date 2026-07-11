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

// spotless:off
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;

import carpet.CarpetServer;

import cn.nm.lms.carpetlmsaddition.Mod;
import cn.nm.lms.carpetlmsaddition.lib.Utils;

//#if MC>=260300
import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeMap;
//#endif
// spotless:on

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

    //#if MC>=260300
    public static RecipeMap mergeWithManagedRecipes(RecipeMap original) {
        return RecipeMap.create(new ManagedRecipeLookup(mergeWithManagedRecipes(original.values())));
    }
    //#endif

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

    public static void flushPendingReload() {
        if (!startupReloadPending) {
            return;
        }
        if (reloadRecipes()) {
            startupReloadPending = false;
        }
    }

    private static boolean reloadRecipes() {
        // carpet.conf loading may happen before Carpet script server is initialized.
        // Triggering reloadResources too early would crash in Carpet command re-registering.
        if (CarpetServer.scriptServer == null) {
            return false;
        }
        MinecraftServer server = Utils.getServer();
        server.execute(() -> server.reloadResources(server.getPackRepository().getSelectedIds())
            .thenRun(() -> server.execute(() -> RecipeBookHelper.syncOnlinePlayers(server)))
            .exceptionally(throwable -> {
                Mod.LOGGER.error("Failed to reload resources after recipe rule changed", throwable);
                return null;
            }));
        return true;
    }

    //#if MC>=260300
    private static final class ManagedRecipeLookup implements HolderLookup<Recipe<?>> {
        private final Map<ResourceKey<Recipe<?>>, Holder.Reference<Recipe<?>>> recipes;

        private ManagedRecipeLookup(Iterable<RecipeHolder<?>> recipes) {
            this.recipes = new LinkedHashMap<>();
            for (RecipeHolder<?> recipeHolder : recipes) {
                Recipe<?> recipe = recipeHolder.value();
                this.recipes.put(recipeHolder.id(), new ManagedRecipeReference(this, recipeHolder.id(), recipe));
            }
        }

        @Override
        public Optional<Holder.Reference<Recipe<?>>> get(ResourceKey<Recipe<?>> key) {
            return Optional.ofNullable(recipes.get(key));
        }

        @Override
        public Optional<net.minecraft.core.HolderSet.Named<Recipe<?>>> get(TagKey<Recipe<?>> tagKey) {
            return Optional.empty();
        }

        @Override
        public Stream<Holder.Reference<Recipe<?>>> listElements() {
            return recipes.values().stream();
        }

        @Override
        public Stream<net.minecraft.core.HolderSet.Named<Recipe<?>>> listTags() {
            return Stream.empty();
        }
    }

    private static final class ManagedRecipeReference extends Holder.Reference<Recipe<?>> {
        private ManagedRecipeReference(HolderOwner<Recipe<?>> owner, ResourceKey<Recipe<?>> key, Recipe<?> recipe) {
            super(Type.STAND_ALONE, owner, key, recipe);
        }
    }
    //#endif
}
