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
package cn.nm.lms.carpetlmsaddition.translations;

import java.util.Map;

public final class Translations {
    private static final TranslationService SERVICE = new TranslationService();

    private Translations() {}

    public static void init() {
        SERVICE.init();
    }

    public static Map<String, String> translations(String lang) {
        return SERVICE.translations(lang);
    }

    public static String tr(String key, Object... args) {
        return SERVICE.tr(key, args);
    }

    public static String tr(FeedbackMessage message) {
        return SERVICE.tr(message);
    }

    public static String messageTr(String key, Object... args) {
        return tr("carpetlmsaddition.message." + key, args);
    }

    public record FeedbackMessage(String key, Object... args) {
    }
}
