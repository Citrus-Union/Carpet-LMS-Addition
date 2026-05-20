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
package cn.nm.lms.carpetlmsaddition.storage.getitem;

import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.rule.Settings;

public final class GetItemBotHelper {
    public static final int BOT_SCAN_LIMIT = 4096;

    private GetItemBotHelper() {}

    public static String getBotPrefix() {
        String prefix = Settings.getItemBotPrefix;
        if (prefix == null) {
            return "bot_getitem_";
        }
        String trimmed = prefix.trim();
        return trimmed.isEmpty() ? "bot_getitem_" : trimmed;
    }

    public static boolean isBotOnline(String botName) {
        return Utils.runOnServerThread(() -> Utils.getServer().getPlayerList().getPlayerByName(botName) != null);
    }
}
