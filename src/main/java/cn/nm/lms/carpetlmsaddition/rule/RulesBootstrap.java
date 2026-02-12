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
package cn.nm.lms.carpetlmsaddition.rule;

import carpet.CarpetServer;

import cn.nm.lms.carpetlmsaddition.rule.block.breaking.ExplosionProofBuddingAmethystRule;
import cn.nm.lms.carpetlmsaddition.rule.block.breaking.FragileTrialSpawnerRule;
import cn.nm.lms.carpetlmsaddition.rule.block.breaking.FragileVaultRule;
import cn.nm.lms.carpetlmsaddition.rule.block.breaking.SoftTrialSpawnerRule;
import cn.nm.lms.carpetlmsaddition.rule.block.breaking.SoftVaultRule;
import cn.nm.lms.carpetlmsaddition.rule.block.breaking.UnbreakableBuddingAmethystRule;
import cn.nm.lms.carpetlmsaddition.rule.block.util.dispenserbartering.DispenserBarteringRule;
import cn.nm.lms.carpetlmsaddition.rule.block.util.vaultmaxblacklistsize.VaultMaxBlacklistSizeRule;
import cn.nm.lms.carpetlmsaddition.rule.bugfix.zombifiedpiglinspawnfix.ZombifiedPiglinSpawnFixRule;
import cn.nm.lms.carpetlmsaddition.rule.entity.allay.AllayHealIntervalRule;
import cn.nm.lms.carpetlmsaddition.rule.entity.projectile.PearlIgnoreEntityCollisionRule;
import cn.nm.lms.carpetlmsaddition.rule.entity.projectile.PearlNoTpRule;
import cn.nm.lms.carpetlmsaddition.rule.entity.shulker.ShulkerDupLowHealthFailChanceRule;
import cn.nm.lms.carpetlmsaddition.rule.entity.shulker.ShulkerDupNearbyLimitRule;
import cn.nm.lms.carpetlmsaddition.rule.recipes.elytrarecipe.ElytraRecipeRule;
import cn.nm.lms.carpetlmsaddition.rule.recipes.enchantedgoldenapplerecipe.EnchantedGoldenAppleRecipeRule;
import cn.nm.lms.carpetlmsaddition.rule.recipes.spongerecipe.SpongeRecipeRule;
import cn.nm.lms.carpetlmsaddition.rule.util.chunkloader.helmetcontrolsplayerdistance.HelmetControlsPlayerDistanceRule;
import cn.nm.lms.carpetlmsaddition.rule.util.chunkloader.minecartchunkloader.MinecartChunkLoaderRule;
import cn.nm.lms.carpetlmsaddition.rule.util.command.commandlms.CommandLMSRule;
import cn.nm.lms.carpetlmsaddition.rule.util.command.opplayernocheatextra.OpPlayerNoCheatExtraRule;
import cn.nm.lms.carpetlmsaddition.rule.util.command.playercommanddropall.PlayerCommandDropallRule;
import cn.nm.lms.carpetlmsaddition.rule.util.helper.lowhealthspectator.LowHealthSpectatorRule;
import cn.nm.lms.carpetlmsaddition.rule.util.looting.minimallootinglevel.MinimalLootingLevelRule;

public final class RulesBootstrap
{
    public static void registerAll()
    {
        Class<?>[] settingsClasses = new Class<?>[]{
                AllayHealIntervalRule.class,
                CommandLMSRule.class,
                DispenserBarteringRule.class,
                ElytraRecipeRule.class,
                EnchantedGoldenAppleRecipeRule.class,
                ExplosionProofBuddingAmethystRule.class,
                FragileTrialSpawnerRule.class,
                FragileVaultRule.class,
                HelmetControlsPlayerDistanceRule.class,
                LowHealthSpectatorRule.class,
                MinecartChunkLoaderRule.class,
                MinimalLootingLevelRule.class,
                OpPlayerNoCheatExtraRule.class,
                PearlIgnoreEntityCollisionRule.class,
                PearlNoTpRule.class,
                PlayerCommandDropallRule.class,
                ShulkerDupLowHealthFailChanceRule.class,
                ShulkerDupNearbyLimitRule.class,
                SoftTrialSpawnerRule.class,
                SoftVaultRule.class,
                SpongeRecipeRule.class,
                UnbreakableBuddingAmethystRule.class,
                VaultMaxBlacklistSizeRule.class,
                ZombifiedPiglinSpawnFixRule.class
        };
        for (
            Class<?> settingsClass : settingsClasses
        )
        {
            CarpetServer.settingsManager.parseSettingsClass(settingsClass);
        }
    }
}
