package app.simplecloud.prefixes.shared.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class SyncConfig(
    val tablist: SyncTargets = SyncTargets(),
    val chat: SyncTargets = SyncTargets()
)
