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
package cn.nm.lms.carpetlmsaddition.rule.recipes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

import cn.nm.lms.carpetlmsaddition.CarpetLMSAdditionMod;
import cn.nm.lms.carpetlmsaddition.rule.recipes.elytrarecipe.CraftableElytra;
import cn.nm.lms.carpetlmsaddition.rule.recipes.enchantedgoldenapplerecipe.CraftableEnchantedGoldenApple;
import cn.nm.lms.carpetlmsaddition.rule.recipes.spongerecipe.CraftableSponge;

public final class RecipesInit
{
    public static final RecipeSerializer<CraftableElytra> CRAFTABLE_ELYTRA = CompatRecipeSerializer.simple(
            CraftableElytra::new
    );
    public static final RecipeSerializer<CraftableEnchantedGoldenApple> CRAFTABLE_ENCHANTED_GOLDEN_APPLE = CompatRecipeSerializer.simple(
            CraftableEnchantedGoldenApple::new
    );
    public static final RecipeSerializer<CraftableSponge> CRAFTABLE_SPONGE = CompatRecipeSerializer.simple(
            CraftableSponge::new
    );

    public static void init()
    {
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(CarpetLMSAdditionMod.MOD_ID, "craftableelytra"),
                CRAFTABLE_ELYTRA
        );
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(
                        CarpetLMSAdditionMod.MOD_ID,
                        "craftableenchantedgoldenapple"
                ),
                CRAFTABLE_ENCHANTED_GOLDEN_APPLE
        );
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(CarpetLMSAdditionMod.MOD_ID, "craftablesponge"),
                CRAFTABLE_SPONGE
        );
    }
}
