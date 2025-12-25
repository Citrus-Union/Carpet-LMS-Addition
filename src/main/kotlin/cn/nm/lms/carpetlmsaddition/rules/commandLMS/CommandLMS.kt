package cn.nm.lms.carpetlmsaddition.rules.commandLMS

import carpet.utils.CommandHelper
import cn.nm.lms.carpetlmsaddition.lib.PlayerConfig
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

object CommandLMS {
    @JvmField
    val ALL_CONFIG: Map<String, Set<String>> =
        mapOf(
            "lowHealthSpectator" to setOf("true", "false"),
        )

    private fun configList(): Set<String> = ALL_CONFIG.keys

    private fun valuesOf(config: String): Set<String> = ALL_CONFIG[config] ?: emptySet()

    private fun hasConfig(config: String) = configList().contains(config)

    private fun hasValue(
        config: String,
        value: String,
    ) = hasConfig(config) && valuesOf(config).contains(value)

    private fun canUse(
        src: ServerCommandSource,
        target: ServerPlayerEntity,
    ): Boolean {
        val self = src.player
        val isSelf = (self != null && self.uuid == target.uuid)
        val perm = if (isSelf) CommandLMSSelf.commandLMSSelf else CommandLMSOthers.commandLMSOthers
        return CommandHelper.canUseCommand(src, perm)
    }

    fun register() {
        CommandRegistrationCallback.EVENT.register(
            CommandRegistrationCallback { dispatcher, _, _ ->
                dispatcher.register(
                    CommandManager
                        .literal("lms")
                        .then(
                            CommandManager
                                .argument("player", EntityArgumentType.player())
                                .then(
                                    CommandManager
                                        .argument("config", StringArgumentType.word())
                                        .suggests { _, builder ->
                                            configList().forEach { builder.suggest(it) }
                                            builder.buildFuture()
                                        }.executes { ctx ->
                                            val src = ctx.source
                                            val target = EntityArgumentType.getPlayer(ctx, "player")
                                            val config = StringArgumentType.getString(ctx, "config")
                                            if (!hasConfig(config)) {
                                                src.sendError(Text.literal("Unknown config: $config"))
                                                return@executes 0
                                            }
                                            if (!canUse(src, target)) {
                                                src.sendError(Text.literal("No permission"))
                                                return@executes 0
                                            }
                                            val raw = PlayerConfig.get(target.uuid, config)
                                            src.sendFeedback(
                                                { Text.literal("[Carpet LMS Addition] $config = ${raw ?: "null"}") },
                                                false,
                                            )
                                            1
                                        }.then(
                                            CommandManager
                                                .argument("value", StringArgumentType.word())
                                                .suggests { ctx, builder ->
                                                    val config = StringArgumentType.getString(ctx, "config")
                                                    valuesOf(config).forEach { builder.suggest(it) }
                                                    builder.buildFuture()
                                                }.executes { ctx ->
                                                    val src = ctx.source
                                                    val target = EntityArgumentType.getPlayer(ctx, "player")
                                                    val config = StringArgumentType.getString(ctx, "config")
                                                    val value = StringArgumentType.getString(ctx, "value")
                                                    if (!hasValue(config, value)) {
                                                        src.sendError(Text.literal("Unknown config or value: $config $value"))
                                                        return@executes 0
                                                    }
                                                    if (!canUse(src, target)) {
                                                        src.sendError(Text.literal("No permission"))
                                                        return@executes 0
                                                    }
                                                    PlayerConfig.set(target.uuid, config, value)
                                                    src.sendFeedback(
                                                        { Text.literal("[Carpet LMS Addition] $config = $value") },
                                                        false,
                                                    )
                                                    1
                                                },
                                        ),
                                ),
                        ),
                )
            },
        )
    }
}
