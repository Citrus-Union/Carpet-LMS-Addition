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

import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;
import carpet.api.settings.Validators;

import cn.nm.lms.carpetlmsaddition.lib.PlayerConfig;
import cn.nm.lms.carpetlmsaddition.rule.block.dispenser.bartering.DispenserBarteringSetting;
import cn.nm.lms.carpetlmsaddition.rule.recipe.observer.RecipeRuleObserver;
import cn.nm.lms.carpetlmsaddition.rule.recipe.smelting.ShulkerBoxFurnaceSetting;
import cn.nm.lms.carpetlmsaddition.rule.util.helper.LowHealthSpectatorMethod;

public final class Settings {
    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static int allayHealInterval = -1;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.COMMAND},
        validators = {Validators.CommandLevel.class})
    public static String commandLMSSelf = "true";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.COMMAND},
        validators = {Validators.CommandLevel.class})
    public static String commandLMSOthers = "ops";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE})
    public static DispenserBarteringSetting dispenserBartering = DispenserBarteringSetting.FALSE;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE}, options = {"true", "false"},
        strict = false)
    public static String dispenserBarteringName = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL}, validators = {RecipeRuleObserver.class})
    public static boolean elytraRecipe = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL}, validators = {RecipeRuleObserver.class})
    public static boolean enchantedGoldenAppleRecipe = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static boolean explosionProofBuddingAmethyst = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static boolean fragileTrialSpawner = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static boolean fragileVault = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE})
    public static boolean globalSlimeChunk = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static PlayerConfig.RuleSetting lowHealthSpectator = PlayerConfig.RuleSetting.FALSE;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static LowHealthSpectatorMethod lowHealthSpectatorMethod = LowHealthSpectatorMethod.VANILLA;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL}, validators = {Validators.NonNegativeNumber.class})
    public static long lowHealthSpectatorCooldown = 200;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL}, validators = {Validators.NonNegativeNumber.class},
        options = {"5", "10", "15", "20"}, strict = false)
    public static float lowHealthSpectatorThreshold = 5;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.COMMAND})
    public static boolean opPlayerNoCheatExtra = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static boolean pearlIgnoreEntityCollision = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.CREATIVE}, options = {"true", "false"}, strict = false)
    public static String pearlNoTp = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.COMMAND},
        validators = {Validators.CommandLevel.class})
    public static String playerCommandDropall = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE})
    public static ShulkerBoxFurnaceSetting shulkerBoxFurnace = ShulkerBoxFurnaceSetting.FALSE;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static int shulkerDupLowHealthFailChance = -1;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static float shulkerDupNearbyLimit = -1.0F;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static boolean softTrialSpawner = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static boolean softVault = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL}, validators = {RecipeRuleObserver.class})
    public static boolean spongeRecipe = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static boolean unbreakableBuddingAmethyst = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static int vaultMaxBlacklistSize = -1;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.BUGFIX})
    public static boolean zombifiedPiglinSpawnFix = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.COMMAND, RuleCategory.SURVIVAL, RuleCategory.CREATIVE,
        LMSRuleCategory.STORAGE}, validators = {Validators.CommandLevel.class})
    public static String commandStorageWebsite = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.COMMAND, RuleCategory.SURVIVAL, RuleCategory.CREATIVE},
        validators = {Validators.CommandLevel.class})
    public static String commandSetPassword = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.COMMAND, RuleCategory.SURVIVAL, RuleCategory.CREATIVE,
        LMSRuleCategory.STORAGE}, validators = {Validators.CommandLevel.class})
    public static String commandGetItem = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.COMMAND, RuleCategory.SURVIVAL, RuleCategory.CREATIVE,
        LMSRuleCategory.STORAGE}, validators = {Validators.CommandLevel.class})
    public static String commandGetStorageData = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.COMMAND, RuleCategory.SURVIVAL, RuleCategory.CREATIVE,
        LMSRuleCategory.STORAGE}, validators = {Validators.CommandLevel.class})
    public static String commandCleanGetItemBot = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE, LMSRuleCategory.STORAGE})
    public static boolean websiteGetItem = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE, LMSRuleCategory.STORAGE})
    public static String getItemBotPrefix = "bot_getitem_";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE, LMSRuleCategory.STORAGE},
        validators = {Validators.NonNegativeNumber.class})
    public static int getItemDelayMs = 0;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE, LMSRuleCategory.STORAGE},
        validators = {Validators.NonNegativeNumber.class})
    public static int getItemCooldownSeconds = 0;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE, LMSRuleCategory.STORAGE},
        validators = {Validators.NonNegativeNumber.class})
    public static int getStorageDataCooldownSeconds = 0;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE, LMSRuleCategory.STORAGE},
        validators = {Validators.NonNegativeNumber.class})
    public static int websiteLoginCooldownSeconds = 0;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE, LMSRuleCategory.STORAGE},
        validators = {Validators.NonNegativeNumber.class})
    public static int getItemMaxCount = 0;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE, LMSRuleCategory.STORAGE},
        validators = {Validators.NonNegativeNumber.class})
    public static int getItemMaxBots = 0;

    //#if MC>=12102
    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE},
        options = {"origin", "1.21.9+", "1.21.8-"})
    public static String entityTeleportCrossDimension = "origin";
    //#endif
}
