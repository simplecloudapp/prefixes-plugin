package app.simplecloud.prefixes.shared.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class CommandHelpMessages(
    val title: String = "<prefix> <#0EA5E9>SimpleCloud Prefixes commands",
    val entry: String = "<#E2E8F0><command>"
)
