package cn.nm.lms.carpetlmsaddition.rules.lowhealthspectator

import cn.nm.lms.carpetlmsaddition.CarpetLMSAdditionMod
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.server.network.ServerPlayerEntity
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
                val last = cooldownMap[player.uuid] ?: (now - LowHealthSpectatorCooldown.lowHealthSpectatorCooldown)

                if (now - last < LowHealthSpectatorCooldown.lowHealthSpectatorCooldown) {
                    return@AfterDamage
                }

                val hp = player.health

                if (
                    LowHealthSpectator.lowHealthSpectator == "true" &&
                    hp > 0f &&
                    hp <= LowHealthSpectatorThreshold.lowHealthSpectatorThreshold.toFloat() &&
                    !player.isSpectator
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

                        "carpet" -> {
                            // TODO
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
}
