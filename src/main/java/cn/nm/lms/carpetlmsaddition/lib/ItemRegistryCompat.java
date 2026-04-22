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
package cn.nm.lms.carpetlmsaddition.lib;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import org.jspecify.annotations.Nullable;

public final class ItemRegistryCompat {
    private ItemRegistryCompat() {}

    @Nullable
    public static Item getItem(Identifier itemId) {
        //#if MC>=12102
        var itemReference = BuiltInRegistries.ITEM.get(itemId);
        if (itemReference.isEmpty()) {
            return null;
        }
        return itemReference.get().value();
        //#else
        //$$ if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
        //$$     return null;
        //$$ }
        //$$ return BuiltInRegistries.ITEM.get(itemId);
        //#endif
    }
}
