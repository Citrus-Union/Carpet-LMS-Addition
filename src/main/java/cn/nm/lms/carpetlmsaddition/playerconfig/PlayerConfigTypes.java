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
package cn.nm.lms.carpetlmsaddition.playerconfig;

import java.util.function.BooleanSupplier;

public final class PlayerConfigTypes {
    private PlayerConfigTypes() {}

    public static BooleanPlayerConfig bool(String key, BooleanSupplier enabled) {
        return new BooleanPlayerConfig(key, enabled);
    }

    public static <E extends Enum<E>> EnumPlayerConfig<E> enumConfig(String key, Class<E> enumClass,
        BooleanSupplier enabled) {
        return new EnumPlayerConfig<>(key, enumClass, enabled);
    }

    public static StringSetPlayerConfig stringSet(String key, String argumentName, BooleanSupplier enabled) {
        return new StringSetPlayerConfig(key, argumentName, enabled);
    }

    public static BlockSetPlayerConfig blockSet(String key, BooleanSupplier enabled) {
        return new BlockSetPlayerConfig(key, enabled);
    }
}
