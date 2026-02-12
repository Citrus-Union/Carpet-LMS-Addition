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
package cn.nm.lms.carpetlmsaddition.rule.util.chunkloader.minecartchunkloader;

import net.minecraft.server.level.TicketType;

import cn.nm.lms.carpetlmsaddition.rule.util.chunkloader.Tickets;

public enum MinecartChunkLoaderInit
{
    MINECART_1(1), MINECART_20(20), MINECART_300(300);

    private final int timeout;
    private final TicketType ticket;

    MinecartChunkLoaderInit(int timeout)
    {
        this.timeout = timeout;
        this.ticket = Tickets.register(name().toLowerCase(), timeout);
    }

    public static TicketType getTicket(long timeout)
    {
        for (
            MinecartChunkLoaderInit value : values()
        )
        {
            if (value.timeout == timeout)
            {
                return value.ticket;
            }
        }

        throw new IllegalArgumentException("Invalid timeout value: " + timeout);
    }

    public static void init()
    {
    }
}
