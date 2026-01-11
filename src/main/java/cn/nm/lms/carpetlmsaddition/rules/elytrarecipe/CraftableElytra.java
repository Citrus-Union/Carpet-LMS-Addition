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
package cn.nm.lms.carpetlmsaddition.rules.elytrarecipe;

import java.util.Arrays;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import cn.nm.lms.carpetlmsaddition.CarpetLMSAdditionRecipes;
import cn.nm.lms.carpetlmsaddition.lib.recipe.ShapedRecipe;

public class CraftableElytra extends ShapedRecipe
{
    public CraftableElytra(CraftingBookCategory category)
    {
        super(
                category,
                () -> ElytraRecipe.elytraRecipe,
                3,
                3,
                Arrays.asList(
                        Items.PHANTOM_MEMBRANE,
                        Items.END_CRYSTAL,
                        Items.PHANTOM_MEMBRANE,
                        Items.PHANTOM_MEMBRANE,
                        Items.ELYTRA,
                        Items.PHANTOM_MEMBRANE,
                        Items.PHANTOM_MEMBRANE,
                        Items.END_CRYSTAL,
                        Items.PHANTOM_MEMBRANE
                ),
                Items.ELYTRA,
                1,
                (CraftingInput input) ->
                {
                    NonNullList<ItemStack> rem = NonNullList.withSize(
                            input.size(),
                            ItemStack.EMPTY
                    );
                    for (
                            int i = 0;
                            i < input.size();
                            i++
                    )
                    {
                        ItemStack stack = input.getItem(i);
                        if (stack.is(Items.ELYTRA))
                        {
                            ItemStack copy = stack.copy();
                            copy.setCount(1);
                            rem.set(i, copy);
                        }
                    }
                    return rem;
                }
        );
    }

    @Override
    protected RecipeSerializer<? extends CustomRecipe> getSerializer0()
    {
        return CarpetLMSAdditionRecipes.CRAFTABLE_ELYTRA;
    }
}
