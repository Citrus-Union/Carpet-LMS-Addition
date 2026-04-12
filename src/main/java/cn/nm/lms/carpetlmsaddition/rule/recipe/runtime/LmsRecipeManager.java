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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import cn.nm.lms.carpetlmsaddition.Mod;
import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.recipe.crafting.CraftableElytra;
import cn.nm.lms.carpetlmsaddition.rule.recipe.crafting.CraftableEnchantedGoldenApple;
import cn.nm.lms.carpetlmsaddition.rule.recipe.crafting.CraftableSponge;

public final class LmsRecipeManager {
    private static final Map<Object, RecipeHolder<?>> CUSTOM_RECIPES = new LinkedHashMap<>();
    private static final Map<Object, RecipeHolder<?>> ALL_RECIPES = new LinkedHashMap<>();

    public static synchronized Collection<RecipeHolder<?>> getCustomRecipes() {
        CUSTOM_RECIPES.clear();
        buildRecipes();
        return new ArrayList<>(CUSTOM_RECIPES.values());
    }

    public static synchronized Collection<RecipeHolder<?>> getAllManagedRecipes() {
        ALL_RECIPES.clear();
        buildAllRecipes();
        return new ArrayList<>(ALL_RECIPES.values());
    }

    public static synchronized void rebuildRecipes() {
        CUSTOM_RECIPES.clear();
        buildRecipes();
    }

    public static synchronized void clearRecipes() {
        CUSTOM_RECIPES.clear();
    }

    private static void buildRecipes() {
        create(() -> Settings.elytraRecipe, "craftableelytra",
            () -> new CraftableElytra(CraftingBookCategory.EQUIPMENT));
        create(() -> Settings.enchantedGoldenAppleRecipe, "craftableenchantedgoldenapple",
            () -> new CraftableEnchantedGoldenApple(CraftingBookCategory.MISC));
        create(() -> Settings.spongeRecipe, "craftablesponge",
            () -> new CraftableSponge(CraftingBookCategory.BUILDING));
    }

    private static void buildAllRecipes() {
        createAll("craftableelytra", () -> new CraftableElytra(CraftingBookCategory.EQUIPMENT));
        createAll("craftableenchantedgoldenapple", () -> new CraftableEnchantedGoldenApple(CraftingBookCategory.MISC));
        createAll("craftablesponge", () -> new CraftableSponge(CraftingBookCategory.BUILDING));
    }

    private static void create(BooleanSupplier ruleEnabled, String recipeId,
        Supplier<? extends Recipe<?>> recipeFactory) {
        if (!ruleEnabled.getAsBoolean()) {
            return;
        }
        RecipeHolder<?> holder = createHolder(recipeId, recipeFactory);
        CUSTOM_RECIPES.put(holder.id(), holder);
    }

    private static void createAll(String recipeId, Supplier<? extends Recipe<?>> recipeFactory) {
        RecipeHolder<?> holder = createHolder(recipeId, recipeFactory);
        ALL_RECIPES.put(holder.id(), holder);
    }

    private static RecipeHolder<?> createHolder(String recipeId, Supplier<? extends Recipe<?>> recipeFactory) {
        //#if MC>=12102
        Identifier id = Identifier.fromNamespaceAndPath(Mod.MOD_ID, recipeId);
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
        return new RecipeHolder<>(key, recipeFactory.get());
        //#else
        //$$ ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Mod.MOD_ID, recipeId);
        //$$ return new RecipeHolder<>(id, recipeFactory.get());
        //#endif
    }
}
