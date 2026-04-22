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

import cn.nm.lms.carpetlmsaddition.rule.recipe.observer.RecipeRuleObserver;

public final class Settings {
    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL}, validators = {Validators.NonNegativeNumber.class})
    public static int allayHealInterval = 10;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.COMMAND},
        validators = {Validators.CommandLevel.class})
    public static String commandLMSSelf = "true";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.COMMAND},
        validators = {Validators.CommandLevel.class})
    public static String commandLMSOthers = "ops";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE},
        options = {"false", "ingot", "block", "shulkerBox"})
    public static String dispenserBartering = "false";

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

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE})
    public static boolean helmetControlsPlayerDistance = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL}, options = {"true", "false", "custom"})
    public static String lowHealthSpectator = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL},
        options = {"vanilla", "mcdreforged", "carpet-org-addition", "kick"})
    public static String lowHealthSpectatorMethod = "vanilla";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL}, validators = {Validators.NonNegativeNumber.class})
    public static long lowHealthSpectatorCooldown = 200;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL}, validators = {Validators.NonNegativeNumber.class},
        options = {"5", "10", "15", "20"}, strict = false)
    public static float lowHealthSpectatorThreshold = 5;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL}, options = {"0", "1", "20", "300"})
    public static int minecartChunkLoader = 0;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.COMMAND})
    public static boolean opPlayerNoCheatExtra = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static boolean pearlIgnoreEntityCollision = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.CREATIVE}, options = {"true", "false"}, strict = false)
    public static String pearlNoTp = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.COMMAND},
        validators = {Validators.CommandLevel.class})
    public static String playerCommandDropall = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE},
        options = {"false", "force", "strict"})
    public static String shulkerBoxFurnace = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL}, validators = {Validators.NonNegativeNumber.class})
    public static int shulkerDupLowHealthFailChance = 4;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL}, validators = {Validators.NonNegativeNumber.class})
    public static int shulkerDupNearbyLimit = 5;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static boolean softTrialSpawner = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static boolean softVault = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL}, validators = {RecipeRuleObserver.class})
    public static boolean spongeRecipe = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL})
    public static boolean unbreakableBuddingAmethyst = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL}, validators = {Validators.NonNegativeNumber.class})
    public static int vaultMaxBlacklistSize = 128;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.BUGFIX})
    public static boolean zombifiedPiglinSpawnFix = false;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.COMMAND, RuleCategory.SURVIVAL, RuleCategory.CREATIVE,
        LMSRuleCategory.STORAGE}, validators = {Validators.CommandLevel.class})
    public static String commandCheckStorageData = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE, LMSRuleCategory.STORAGE},
        validators = {Validators.NonNegativeNumber.class})
    public static int checkStorageAutoUpdateDataInterval = 0;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.COMMAND, RuleCategory.SURVIVAL, RuleCategory.CREATIVE,
        LMSRuleCategory.STORAGE}, validators = {Validators.CommandLevel.class})
    public static String commandCheckStorageServer = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.COMMAND, RuleCategory.SURVIVAL, RuleCategory.CREATIVE},
        validators = {Validators.CommandLevel.class})
    public static String commandSetPassword = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.COMMAND, RuleCategory.SURVIVAL, RuleCategory.CREATIVE,
        LMSRuleCategory.STORAGE}, validators = {Validators.CommandLevel.class})
    public static String commandGetItem = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.COMMAND, RuleCategory.SURVIVAL, RuleCategory.CREATIVE,
        LMSRuleCategory.STORAGE}, validators = {Validators.CommandLevel.class})
    public static String commandCleanGetItemBot = "false";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE, LMSRuleCategory.STORAGE})
    public static String getItemBotPrefix = "bot_getitem_";

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE, LMSRuleCategory.STORAGE},
        validators = {Validators.NonNegativeNumber.class})
    public static int getItemDelayMs = 50;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE, LMSRuleCategory.STORAGE},
        validators = {Validators.NonNegativeNumber.class})
    public static int getItemCooldownSeconds = 0;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE, LMSRuleCategory.STORAGE},
        validators = {Validators.NonNegativeNumber.class})
    public static int getItemMaxCount = 0;

    @Rule(categories = {LMSRuleCategory.LMS, RuleCategory.SURVIVAL, RuleCategory.CREATIVE, LMSRuleCategory.STORAGE},
        validators = {Validators.NonNegativeNumber.class})
    public static int getItemMaxBots = 0;
}
