package cn.nm.lms.carpetlmsaddition.rules

import carpet.CarpetServer
import cn.nm.lms.carpetlmsaddition.rules.commandLMS.CommandLMSOthers
import cn.nm.lms.carpetlmsaddition.rules.commandLMS.CommandLMSSelf
import cn.nm.lms.carpetlmsaddition.rules.elytrarecipe.ElytraRecipe
import cn.nm.lms.carpetlmsaddition.rules.enchantedgoldenapplerecipe.EnchantedGoldenAppleRecipe
import cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator.LowHealthSpectator
import cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator.LowHealthSpectatorCooldown
import cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator.LowHealthSpectatorMethod
import cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator.LowHealthSpectatorThreshold
import cn.nm.lms.carpetlmsaddition.rules.minimallootinglevel.MinimalLootingLevel
import cn.nm.lms.carpetlmsaddition.rules.playercommanddropall.PlayerCommandDropall
import cn.nm.lms.carpetlmsaddition.rules.spongerecipe.SpongeRecipe

object RulesBootstrap {
    fun registerAll() {
        listOf(
            AllayHealInterval::class.java,
            CommandLMSOthers::class.java,
            CommandLMSSelf::class.java,
            ElytraRecipe::class.java,
            EnchantedGoldenAppleRecipe::class.java,
            FragileVault::class.java,
            LowHealthSpectator::class.java,
            LowHealthSpectatorCooldown::class.java,
            LowHealthSpectatorMethod::class.java,
            LowHealthSpectatorThreshold::class.java,
            MinimalLootingLevel::class.java,
            PearlIgnoreEntityCollision::class.java,
            PlayerCommandDropall::class.java,
            SoftVault::class.java,
            SpongeRecipe::class.java,
            ZombifiedPiglinSpawnFix::class.java,
        ).forEach { settingsClass ->
            CarpetServer.settingsManager.parseSettingsClass(settingsClass)
        }
    }
}
