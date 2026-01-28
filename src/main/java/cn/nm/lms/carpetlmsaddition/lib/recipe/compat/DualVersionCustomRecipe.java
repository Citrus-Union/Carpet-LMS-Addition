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

import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;

public abstract class DualVersionCustomRecipe extends CustomRecipe
{
    protected final CraftingBookCategory category;

    //#if MC>=260100
    protected DualVersionCustomRecipe(CraftingBookCategory category)
    {
        this.category = category;
    }
    //#else
    //$$ protected DualVersionCustomRecipe(CraftingBookCategory category)
    //$$ {
    //$$     super(category);
    //$$     this.category = category;
    //$$ }
    //#endif

    @Override
    public CraftingBookCategory category()
    {
        return category;
    }

    @Override
    public boolean isSpecial()
    {
        return false;
    }

    @Override
    public boolean showNotification()
    {
        return false;
    }

    @Override
    public String group()
    {
        return "";
    }
}
