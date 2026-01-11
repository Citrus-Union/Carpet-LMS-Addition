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
package cn.nm.lms.carpetlmsaddition;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CarpetLMSAdditionTranslations
{
    private static final Map<String, Map<String, String>> TRANSLATION_STORAGE = new LinkedHashMap<>();

    private static final String DEFAULT_LANG = "en_us";
    private static final String RESOURCE_DIR = "assets/carpetlmsaddition/lang";
    private static final Gson GSON = new Gson();

    private CarpetLMSAdditionTranslations()
    {
    }

    public static void loadTranslations()
    {
        List<String> languages;
        try
        {
            languages = readJson(
                    RESOURCE_DIR + "/meta/languages.json", new TypeToken<List<String>>()
                    {
                    }.getType());
        } catch (Exception e)
        {
            CarpetLMSAdditionMod.LOGGER.error("Failed to read language list", e);
            return;
        }

        for (String lang : languages)
        {
            TRANSLATION_STORAGE.computeIfAbsent(lang, key -> new HashMap<>(loadTranslationFile(key)));
        }
    }

    private static Map<String, String> loadTranslationFile(String lang)
    {
        try
        {
            return readJson(
                    RESOURCE_DIR + "/" + lang + ".json", new TypeToken<Map<String, String>>()
                    {
                    }.getType());
        } catch (Exception e)
        {
            String msg = "Failed to load translation of language " + lang;
            CarpetLMSAdditionMod.LOGGER.error(msg, e);
            if (FabricLoader.getInstance().isDevelopmentEnvironment())
            {
                throw new RuntimeException(msg, e);
            }
            return Collections.emptyMap();
        }
    }

    private static Map<String, String> getTranslations(String lang)
    {
        Map<String, String> translations = TRANSLATION_STORAGE.get(lang);
        return translations != null ? translations : Collections.emptyMap();
    }

    public static Map<String, String> translations(String lang)
    {
        Map<String, String> result = new HashMap<>(getTranslations(DEFAULT_LANG));

        if (!DEFAULT_LANG.equals(lang))
        {
            result.putAll(getTranslations(lang));
        }

        return result;
    }

    private static <T> T readJson(String path, Type type)
    {
        ClassLoader classLoader = CarpetLMSAdditionTranslations.class.getClassLoader();
        InputStream stream = classLoader != null ? classLoader.getResourceAsStream(path) : null;
        if (stream == null)
        {
            throw new IllegalStateException("Missing resource: " + path);
        }
        try (InputStream input = stream; InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8))
        {
            return GSON.fromJson(reader, type);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
