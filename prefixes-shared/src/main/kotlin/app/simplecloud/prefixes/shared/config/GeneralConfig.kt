package app.simplecloud.prefixes.shared.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class GeneralConfig(
    val source: SourceType = SourceType.CONFIG,
    val defaultGroup: String = "default",
    val sync: SyncConfig = SyncConfig()
)
