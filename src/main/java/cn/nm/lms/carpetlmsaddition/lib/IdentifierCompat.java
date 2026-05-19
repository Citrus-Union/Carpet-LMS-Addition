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

import net.minecraft.resources.Identifier;

import org.jspecify.annotations.Nullable;

public final class IdentifierCompat {
    private static final String DEFAULT_NAMESPACE = "minecraft";

    private IdentifierCompat() {}

    public static String compactId(String id) {
        if (id.startsWith(DEFAULT_NAMESPACE + ":")) {
            return id.substring((DEFAULT_NAMESPACE + ":").length());
        }
        return id;
    }

    @Nullable
    public static Identifier parseCompactId(String rawId) {
        return create(DEFAULT_NAMESPACE, rawId);
    }

    @Nullable
    public static Identifier parseNamespacedId(String rawId) {
        String[] parts = rawId.split(":", 2);
        if (parts.length != 2) {
            return null;
        }
        return create(parts[0], parts[1]);
    }

    @Nullable
    public static Identifier parseId(String rawId) {
        return rawId.contains(":") ? parseNamespacedId(rawId) : parseCompactId(rawId);
    }

    @Nullable
    private static Identifier create(String namespace, String path) {
        try {
            return Identifier.fromNamespaceAndPath(namespace, path);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
