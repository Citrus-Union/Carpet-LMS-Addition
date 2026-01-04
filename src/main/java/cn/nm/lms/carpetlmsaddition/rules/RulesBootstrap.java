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
package cn.nm.lms.carpetlmsaddition.rules;

import carpet.CarpetServer;
import cn.nm.lms.carpetlmsaddition.rules.commandLMS.CommandLMSOthers;
import cn.nm.lms.carpetlmsaddition.rules.commandLMS.CommandLMSSelf;
import cn.nm.lms.carpetlmsaddition.rules.elytrarecipe.ElytraRecipe;
import cn.nm.lms.carpetlmsaddition.rules.enchantedgoldenapplerecipe.EnchantedGoldenAppleRecipe;
import cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator.LowHealthSpectator;
import cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator.LowHealthSpectatorCooldown;
import cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator.LowHealthSpectatorMethod;
import cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator.LowHealthSpectatorThreshold;
import cn.nm.lms.carpetlmsaddition.rules.minimallootinglevel.MinimalLootingLevel;
import cn.nm.lms.carpetlmsaddition.rules.opplayernocheatextra.OpPlayerNoCheatExtra;
import cn.nm.lms.carpetlmsaddition.rules.playercommanddropall.PlayerCommandDropall;
import cn.nm.lms.carpetlmsaddition.rules.spongerecipe.SpongeRecipe;

public final class RulesBootstrap {
  private RulesBootstrap() {}

  public static void registerAll() {
    Class<?>[] settingsClasses =
        new Class<?>[] {
          AllayHealInterval.class,
          CommandLMSOthers.class,
          CommandLMSSelf.class,
          ElytraRecipe.class,
          EnchantedGoldenAppleRecipe.class,
          FragileVault.class,
          LowHealthSpectator.class,
          LowHealthSpectatorCooldown.class,
          LowHealthSpectatorMethod.class,
          LowHealthSpectatorThreshold.class,
          MinimalLootingLevel.class,
          OpPlayerNoCheatExtra.class,
          PearlIgnoreEntityCollision.class,
          PlayerCommandDropall.class,
          SoftVault.class,
          SpongeRecipe.class,
          ZombifiedPiglinSpawnFix.class
        };

    for (Class<?> settingsClass : settingsClasses) {
      CarpetServer.settingsManager.parseSettingsClass(settingsClass);
    }
  }
}
