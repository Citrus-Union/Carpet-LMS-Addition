package cn.nm.lms.carpetlmsaddition

import carpet.CarpetExtension
import carpet.CarpetServer
import cn.nm.lms.carpetlmsaddition.rules.RulesBootstrap
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object CarpetLMSAdditionMod : ModInitializer, CarpetExtension {
    const val MOD_ID: String = "carpet-lms-addition"
    const val MOD_NAME: String = "Carpet LMS Addition"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MOD_NAME)

    @JvmStatic
    val version: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FabricLoader
            .getInstance()
            .getModContainer(MOD_ID)
            .orElseThrow { RuntimeException("Mod not found: $MOD_ID") }
            .metadata
            .version
            .friendlyString
    }

    override fun onInitialize() {
        LOGGER.info("$MOD_NAME version $version")
        CarpetServer.manageExtension(this)
        CarpetLMSAdditionInit.initall()
    }

    override fun version(): String = version

    override fun onGameStarted() {
        RulesBootstrap.registerAll()
    }

    override fun canHasTranslations(lang: String): Map<String, String> = CarpetLMSAdditionTranslations.getTranslation(lang)
}
