package app.simplecloud.prefixes.shared.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class CommandMessages(
    val help: CommandHelpMessages = CommandHelpMessages(),
    val usage: CommandUsageMessages = CommandUsageMessages(),
    val permission: CommandPermissionMessages = CommandPermissionMessages(),
    val reload: CommandReloadMessages = CommandReloadMessages()
)
