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

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import carpet.CarpetExtension;
import carpet.CarpetServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.nm.lms.carpetlmsaddition.rules.RulesBootstrap;

public class CarpetLMSAdditionMod implements ModInitializer, CarpetExtension
{
    public static final String MOD_ID = "carpet-lms-addition";
    private static volatile ModContainer modContainer;
    public static final Logger LOGGER = LogManager.getLogger(getModName());

    private static ModContainer getModContainer()
    {
        if (modContainer == null)
        {
            synchronized (CarpetLMSAdditionMod.class)
            {
                if (modContainer == null)
                {
                    modContainer = FabricLoader.getInstance()
                                               .getModContainer(MOD_ID)
                                               .orElseThrow(
                                                       () -> new RuntimeException(
                                                               "Mod not found: " + MOD_ID
                                                       )
                                               );
                }
            }
        }
        return modContainer;
    }

    public static String getVersion()
    {
        return getModContainer().getMetadata().getVersion().getFriendlyString();
    }

    public static String getModName()
    {
        return getModContainer().getMetadata().getName();
    }

    @Override
    public void onInitialize()
    {
        LOGGER.info("{} version {}", getModName(), getVersion());
        CarpetServer.manageExtension(this);
        CarpetLMSAdditionInit.initAll();
    }

    @Override
    public String version()
    {
        return getVersion();
    }

    @Override
    public void onGameStarted()
    {
        RulesBootstrap.registerAll();
    }

    @Override
    public java.util.Map<String, String> canHasTranslations(String lang)
    {
        return CarpetLMSAdditionTranslations.translations(lang);
    }
}
