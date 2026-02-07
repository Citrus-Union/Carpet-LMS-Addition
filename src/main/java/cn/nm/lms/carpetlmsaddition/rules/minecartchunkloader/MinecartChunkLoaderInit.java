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
package cn.nm.lms.carpetlmsaddition.rules.minecartchunkloader;

import net.minecraft.server.level.TicketType;

import cn.nm.lms.carpetlmsaddition.lib.Tickets;

public final class MinecartChunkLoaderInit
{
    private static final TicketType T1 = Tickets.register("minecart_1", 1, 15);
    private static final TicketType T20 = Tickets.register("minecart_20", 20, 15);
    private static final TicketType T300 = Tickets.register("minecart_300", 300, 15);

    public static TicketType getTicket(long timeout)
    {
        return switch ((int) timeout)
        {
            case 1 -> T1;
            case 20 -> T20;
            case 300 -> T300;
            default -> throw new IllegalArgumentException("Invalid timeout value: " + timeout);
        };
    }

    public static void init()
    {
    }
}
