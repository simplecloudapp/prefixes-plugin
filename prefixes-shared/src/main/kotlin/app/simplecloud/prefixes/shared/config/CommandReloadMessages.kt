package app.simplecloud.prefixes.shared.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class CommandReloadMessages(
    val success: String = "<prefix> <#A3E635>SimpleCloud Prefixes was successfully reloaded.",
    val failed: String = "<prefix> <#DC2626>SimpleCloud Prefixes could not be reloaded."
)
