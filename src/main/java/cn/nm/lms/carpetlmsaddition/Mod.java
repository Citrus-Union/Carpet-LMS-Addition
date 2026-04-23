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

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import carpet.CarpetExtension;
import carpet.CarpetServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;
import cn.nm.lms.carpetlmsaddition.rule.Bootstrap;
import cn.nm.lms.carpetlmsaddition.rule.recipe.runtime.RecipeBookHelper;
import cn.nm.lms.carpetlmsaddition.rule.recipe.runtime.RecipeRuleHelper;
import cn.nm.lms.carpetlmsaddition.rule.util.command.SetupCommands;
import cn.nm.lms.carpetlmsaddition.rule.util.storage.website.Website;

public class Mod implements ModInitializer, CarpetExtension {
    public static final String MOD_ID = "carpet-lms-addition";
    public static final String COMPACT_NAME = "carpetlmsaddition";
    public static final Logger LOGGER = LogManager.getLogger(getModName());

    public static String getVersion() {
        return ModInfoHolder.VERSION;
    }

    public static String getModName() {
        return ModInfoHolder.NAME;
    }

    public static ModContainer getModContainer() {
        return ModInfoHolder.CONTAINER;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("{} version {}", getModName(), getVersion());
        CarpetServer.manageExtension(this);
        Init.initAll();
    }

    @Override
    public String version() {
        return getVersion();
    }

    @Override
    public void onGameStarted() {
        Bootstrap.registerAll();
    }

    @Override
    public java.util.Map<String, String> canHasTranslations(String lang) {
        return Translations.translations(lang);
    }

    @Override
    public void onServerLoadedWorlds(MinecraftServer server) {
        AsyncTasks.init();
        RecipeRuleHelper.flushPendingReload(server);
        Website.autoStartFromConfigAfterWorldLoaded();
    }

    @Override
    public void onServerClosed(MinecraftServer server) {
        Website.stopServer();
        AsyncTasks.shutdown();
    }

    @Override
    public void onTick(MinecraftServer server) {
        Website.tickAutoUpdateData();
    }

    @Override
    public void onPlayerLoggedIn(ServerPlayer player) {
        RecipeBookHelper.syncPlayer(player);
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher,
        final CommandBuildContext commandBuildContext) {
        SetupCommands.registerAll(dispatcher, commandBuildContext);
    }

    private static class ModInfoHolder {
        private static final ModContainer CONTAINER;
        private static final String NAME;
        private static final String VERSION;

        static {
            CONTAINER = FabricLoader.getInstance().getModContainer(MOD_ID)
                .orElseThrow(() -> new RuntimeException("Mod not found: " + MOD_ID));
            NAME = CONTAINER.getMetadata().getName();
            VERSION = CONTAINER.getMetadata().getVersion().getFriendlyString();
        }
    }
}
