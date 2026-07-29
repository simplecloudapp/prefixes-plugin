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

@ConfigSerializable
data class CommandMessages(
    val help: CommandHelpMessages = CommandHelpMessages(),
    val usage: CommandUsageMessages = CommandUsageMessages(),
    val permission: CommandPermissionMessages = CommandPermissionMessages(),
    val reload: CommandReloadMessages = CommandReloadMessages()
)

@ConfigSerializable
data class CommandHelpMessages(
    val title: String = "<prefix> <#0EA5E9>SimpleCloud Prefixes commands",
    val entry: String = "<#E2E8F0><command>"
)

@ConfigSerializable
data class CommandUsageMessages(
    val invalid: String = "<prefix> <#DC2626>Use <#F8FAFC><command> <#DC2626>instead."
)

@ConfigSerializable
data class CommandPermissionMessages(
    val denied: String = "<prefix> <#DC2626>You do not have permission to use this command."
)

@ConfigSerializable
data class CommandReloadMessages(
    val success: String = "<prefix> <#A3E635>SimpleCloud Prefixes was successfully reloaded.",
    val failed: String = "<prefix> <#DC2626>SimpleCloud Prefixes could not be reloaded."
)
