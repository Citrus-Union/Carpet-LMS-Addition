package cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator

import cn.nm.lms.carpetlmsaddition.CarpetLMSAdditionMod
import cn.nm.lms.carpetlmsaddition.lib.PlayerConfig
import cn.nm.lms.carpetlmsaddition.lib.check.CheckMod
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.world.GameMode
import java.util.UUID

object LowHealthSpectatorController {
    private val cooldownMap = mutableMapOf<UUID, Long>()

    fun init() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(
            ServerLivingEntityEvents.AfterDamage { entity, _, _, _, _ ->
                val player = entity as? ServerPlayerEntity ?: return@AfterDamage
                val world = entity.entityWorld
                val now = world.time
                val playerUUID = player.uuid
                val last = cooldownMap[playerUUID] ?: (now - LowHealthSpectatorCooldown.lowHealthSpectatorCooldown)
                val hp = player.health

                if (
                    isEnabled(playerUUID) &&
                    now - last >= LowHealthSpectatorCooldown.lowHealthSpectatorCooldown &&
                    !player.isSpectator &&
                    hp > 0f &&
                    hp <= LowHealthSpectatorThreshold.lowHealthSpectatorThreshold.toFloat()
                ) {
                    when (LowHealthSpectatorMethod.lowHealthSpectatorMethod) {
                        "vanilla" -> {
                            player.changeGameMode(GameMode.SPECTATOR)
                        }

                        "mcdreforged" -> {
                            CarpetLMSAdditionMod.LOGGER.info(
                                "<{}> !!spec",
                                player.displayName?.string,
                            )
                        }

                        "carpet-org-addition" -> {
                            if (CheckMod.checkMod("carpet-org-addition", ">=1.38.0")) {
                                val server = (world as ServerWorld).server
                                val commandManager = server.commandManager
                                server.execute {
                                    commandManager.parseAndExecute(
                                        player.commandSource,
                                        "spectator",
                                    )
                                }
                            } else {
                                CarpetLMSAdditionMod.LOGGER.warn("Carpet Org Addition (>=1.38.0) not installed")
                            }
                        }

                        "kick" -> {
                            player.networkHandler.disconnect(Text.of("Kicked by Carpet LMS Addition"))
                        }

                        else -> {
                            CarpetLMSAdditionMod.LOGGER.warn(
                                "Unknown lowHealthSpectatorMethod: {}",
                                LowHealthSpectatorMethod.lowHealthSpectatorMethod,
                            )
                        }
                    }
                    cooldownMap[player.uuid] = now
                }
            },
        )
    }

    private fun isEnabled(playerUUID: UUID): Boolean =
        when (LowHealthSpectator.lowHealthSpectator) {
            "true" -> true
            "false" -> false
            "custom" -> PlayerConfig.get(playerUUID, "lowHealthSpectator")?.toBoolean() ?: false
            else -> false
        }
}
