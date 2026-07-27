package app.simplecloud.prefixes.shared.config

import app.simplecloud.plugin.api.shared.config.AbstractMessageConfig
import app.simplecloud.plugin.api.shared.config.VersionedConfig
import app.simplecloud.prefixes.shared.utilities.config.ConfigVersion
import app.simplecloud.prefixes.shared.utilities.config.DefaultConfigs
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MessageConfig(
    override val version: Int = ConfigVersion.VERSION,
    override val variables: Map<String, String> = DefaultConfigs.VARIABLES,
    val command: CommandMessages = CommandMessages()
) : VersionedConfig, AbstractMessageConfig()
