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
package cn.nm.lms.carpetlmsaddition.rule.recipe.crafting;

import java.util.Arrays;
import java.util.Map;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public class CraftableEnchantedGoldenApple extends ShapedRecipe {
    public CraftableEnchantedGoldenApple(CraftingBookCategory category) {
        //#if MC>=260100
        super(new net.minecraft.world.item.crafting.Recipe.CommonInfo(true),
            new net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo(category, ""),
            //#else
            //$$ super("", category,
            //#endif
            ShapedRecipePattern.of(Map.of('G', Ingredient.of(Items.GOLD_BLOCK), 'A', Ingredient.of(Items.GOLDEN_APPLE)),
                Arrays.asList("GGG", "GAG", "GGG")),
            //#if MC>=260100
            new net.minecraft.world.item.ItemStackTemplate(Items.ENCHANTED_GOLDEN_APPLE));
        //#else
        //$$ Items.ENCHANTED_GOLDEN_APPLE.getDefaultInstance());
        //#endif
    }
}
