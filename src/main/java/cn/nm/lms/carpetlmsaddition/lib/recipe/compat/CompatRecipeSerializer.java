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
package cn.nm.lms.carpetlmsaddition.lib.recipe.compat;

import java.util.function.Function;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import com.mojang.serialization.MapCodec;

public final class CompatRecipeSerializer
{
    private CompatRecipeSerializer()
    {
    }

    public static <T extends CustomRecipe> RecipeSerializer<T> simple(
            Function<CraftingBookCategory, T> factory
    )
    {
        //#if MC>=260100
        T sample = factory.apply(CraftingBookCategory.MISC);
        return new RecipeSerializer<>(MapCodec.unit(sample), StreamCodec.unit(sample));
        //#else
        //$$ return new CustomRecipe.Serializer<>(factory::apply);
        //#endif
    }
}
