package app.simplecloud.prefixes.shared.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class ConfigGroup(
    val name: String = "",
    val priority: Int = 0,
    val permission: String = "",
    val prefix: String = "",
    val suffix: String = "",
    val color: String = "",
    val displayName: String = "",
    val chatFormat: String = ""
)
