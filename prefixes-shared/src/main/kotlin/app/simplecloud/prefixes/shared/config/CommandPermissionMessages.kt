package app.simplecloud.prefixes.shared.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class CommandPermissionMessages(
    val denied: String = "<prefix> <#DC2626>You do not have permission to use this command."
)
