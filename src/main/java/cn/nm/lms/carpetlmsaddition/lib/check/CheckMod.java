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
package cn.nm.lms.carpetlmsaddition.lib.check;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

public final class CheckMod
{
    private CheckMod()
    {
    }

    public static boolean checkMod(String modid, String version)
    {
        ModContainer container = FabricLoader.getInstance().getModContainer(modid).orElse(null);
        if (container == null)
        {
            return false;
        }
        try
        {
            VersionPredicate predicate = VersionPredicate.parse(version);
            return predicate.test(container.getMetadata().getVersion());
        }
        catch (VersionParsingException e)
        {
            return false;
        }
    }
}
