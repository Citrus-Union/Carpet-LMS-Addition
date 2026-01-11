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
package cn.nm.lms.carpetlmsaddition.rules.opplayernocheatextra;

import java.util.function.Predicate;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public final class OpPlayerNoCheatExtraHelper
{
    private OpPlayerNoCheatExtraHelper()
    {
    }

    public static Predicate<CommandSourceStack> wrapPredicate(
            Predicate<CommandSourceStack> predicate
    )
    {
        return source -> predicate.test(source) && canCheat(source);
    }

    private static boolean canCheat(CommandSourceStack source)
    {
        return !OpPlayerNoCheatExtra.opPlayerNoCheatExtra || !(source.getEntity() instanceof ServerPlayer);
    }
}
