package app.simplecloud.prefixes.shared.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class CommandUsageMessages(
    val invalid: String = "<prefix> <#DC2626>Use <#F8FAFC><command> <#DC2626>instead."
)
