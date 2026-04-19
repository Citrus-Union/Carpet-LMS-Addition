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

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public final class ChatEventCompat {
    private ChatEventCompat() {}

    public static ClickEvent runCommand(String command) {
        //#if MC>=12105
        return new ClickEvent.RunCommand(command);
        //#else
        //$$ return new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        //#endif
    }

    public static HoverEvent showText(Component text) {
        //#if MC>=12105
        return new HoverEvent.ShowText(text);
        //#else
        //$$ return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
        //#endif
    }
}
