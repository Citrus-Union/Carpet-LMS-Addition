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
package cn.nm.lms.carpetlmsaddition;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CustomRecipe;

import cn.nm.lms.carpetlmsaddition.rules.elytrarecipe.CraftableElytra;
import cn.nm.lms.carpetlmsaddition.rules.enchantedgoldenapplerecipe.CraftableEnchantedGoldenApple;
import cn.nm.lms.carpetlmsaddition.rules.spongerecipe.CraftableSponge;

public final class CarpetLMSAdditionRecipes
{
    public static final CustomRecipe.Serializer<CraftableElytra> CRAFTABLE_ELYTRA = new CustomRecipe.Serializer<>(
            CraftableElytra::new
    );
    public static final CustomRecipe.Serializer<CraftableEnchantedGoldenApple> CRAFTABLE_ENCHANTED_GOLDEN_APPLE = new CustomRecipe.Serializer<>(
            CraftableEnchantedGoldenApple::new
    );
    public static final CustomRecipe.Serializer<CraftableSponge> CRAFTABLE_SPONGE = new CustomRecipe.Serializer<>(
            CraftableSponge::new
    );

    private CarpetLMSAdditionRecipes()
    {
    }

    public static void register()
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
