package cn.nm.lms.carpetlmsaddition.rules

import carpet.CarpetServer
import cn.nm.lms.carpetlmsaddition.rules.craftableSponges.CraftableSponges
import cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator.LowHealthSpectator
import cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator.LowHealthSpectatorCooldown
import cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator.LowHealthSpectatorMethod
import cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator.LowHealthSpectatorThreshold
import cn.nm.lms.carpetlmsaddition.rules.playercommanddropall.PlayerCommandDropall
import cn.nm.lms.carpetlmsaddition.rules.renewableEnchantedGoldenApples.RenewableEnchantedGoldenApples
import cn.nm.lms.carpetlmsaddition.rules.renewableelytra.RenewableElytra

object RulesBootstrap {
    fun registerAll() {
        listOf(
            AllayHealInterval::class.java,
            CraftableSponges::class.java,
            FragileVault::class.java,
            LowHealthSpectator::class.java,
            LowHealthSpectatorCooldown::class.java,
            LowHealthSpectatorMethod::class.java,
            LowHealthSpectatorThreshold::class.java,
            PearlIgnoreEntityCollision::class.java,
            PlayerCommandDropall::class.java,
            RenewableElytra::class.java,
            RenewableEnchantedGoldenApples::class.java,
            SoftVault::class.java,
            ZombifiedPiglinSpawnFix::class.java,
        ).forEach { settingsClass ->
            CarpetServer.settingsManager.parseSettingsClass(settingsClass)
        }
    }
}
