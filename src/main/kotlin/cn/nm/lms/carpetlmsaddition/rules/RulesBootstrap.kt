package cn.nm.lms.carpetlmsaddition.rules

import carpet.CarpetServer
import cn.nm.lms.carpetlmsaddition.rules.playercommanddropall.PlayerCommandDropall
import cn.nm.lms.carpetlmsaddition.rules.renewableelytra.RenewableElytra

object RulesBootstrap {
    fun registerAll() {
        listOf(
            AllayHealInterval::class.java,
            FragileVault::class.java,
            PearlIgnoreEntityCollision::class.java,
            PlayerCommandDropall::class.java,
            RenewableElytra::class.java,
            SoftVault::class.java,
            ZombifiedPiglinSpawnFix::class.java,
        ).forEach { settingsClass ->
            CarpetServer.settingsManager.parseSettingsClass(settingsClass)
        }
    }
}
