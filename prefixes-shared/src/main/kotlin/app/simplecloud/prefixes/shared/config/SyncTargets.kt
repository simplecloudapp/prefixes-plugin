package app.simplecloud.prefixes.shared.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class SyncTargets(
    val enabled: Boolean = true,
    val allServers: Boolean = true,
    val serverGroups: List<String> = emptyList(),
    val persistentServers: List<String> = emptyList()
)
