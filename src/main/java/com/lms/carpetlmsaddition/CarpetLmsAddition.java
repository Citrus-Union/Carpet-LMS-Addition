package com.lms.carpetlmsaddition;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.lms.carpetlmsaddition.lib.CarpetLmsTranslations;
import com.lms.carpetlmsaddition.rules.allayHealInterval.AllayHealRuleSettings;
import com.lms.carpetlmsaddition.rules.banOpPlaceCommand.BanOpPlaceRuleSettings;
import com.lms.carpetlmsaddition.rules.fakePlayerDropAll.DropAllRuleSettings;
import com.lms.carpetlmsaddition.rules.fragileTrialSpawners.FragileTrialSpawnerRuleSettings;
import com.lms.carpetlmsaddition.rules.fragileVaults.FragileVaultRuleSettings;
import com.lms.carpetlmsaddition.rules.pearlIgnoreEntityCollision.PearlRuleSettings;
import java.util.Map;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarpetLmsAddition implements ModInitializer, CarpetExtension {
  public static final String MOD_ID = "carpet_lms_addition";
  public static final String MOD_NAME = "Carpet LMS Addition";
  public static final String VERSION = "1.1.0";
  private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    CarpetServer.manageExtension(this);
    LOGGER.info("Registered Carpet extension {}", MOD_NAME);
  }

  @Override
  public void onGameStarted() {
    CarpetServer.settingsManager.parseSettingsClass(DropAllRuleSettings.class);
    CarpetServer.settingsManager.parseSettingsClass(PearlRuleSettings.class);
    CarpetServer.settingsManager.parseSettingsClass(AllayHealRuleSettings.class);
    CarpetServer.settingsManager.parseSettingsClass(FragileVaultRuleSettings.class);
    CarpetServer.settingsManager.parseSettingsClass(FragileTrialSpawnerRuleSettings.class);
    CarpetServer.settingsManager.parseSettingsClass(BanOpPlaceRuleSettings.class);
    LOGGER.info("Loaded {}", MOD_NAME);
  }

  @Override
  public Map<String, String> canHasTranslations(String lang) {
    return CarpetLmsTranslations.getTranslation(lang);
  }

  @Override
  public String version() {
    return VERSION;
  }
}
