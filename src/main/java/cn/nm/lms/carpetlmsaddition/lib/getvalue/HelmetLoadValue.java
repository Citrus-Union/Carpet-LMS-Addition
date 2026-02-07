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
package cn.nm.lms.carpetlmsaddition.lib.getvalue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import cn.nm.lms.carpetlmsaddition.rules.HelmetControlsPlayerDistance;

public final class HelmetLoadValue
{
    private static final Pattern HELMET_PATTERN = Pattern.compile("^#load(\\d+)$");

    public static int helmetLoadValue(ServerPlayer player)
    {
        if (!HelmetControlsPlayerDistance.helmetControlsPlayerDistance) return 0;
        if (player.isSpectator()) return 0;

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty() || helmet.getItem() != Items.LEATHER_HELMET) return 0;

        String raw = helmet.getHoverName().getString();
        String compact = raw.replaceAll("\\s+", "");
        Matcher m = HELMET_PATTERN.matcher(compact);
        if (!m.matches()) return 0;

        int v;
        try
        {
            v = Integer.parseInt(m.group(1));
        }
        catch (NumberFormatException e)
        {
            return 0;
        }

        return Math.min(v, 31);
    }
}
