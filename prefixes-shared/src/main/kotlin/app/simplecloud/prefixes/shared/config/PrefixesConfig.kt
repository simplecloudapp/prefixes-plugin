package app.simplecloud.prefixes.shared.config

import app.simplecloud.plugin.api.shared.config.VersionedConfig
import app.simplecloud.prefixes.shared.utilities.config.ConfigVersion
import app.simplecloud.prefixes.shared.utilities.config.DefaultConfigs
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class PrefixesConfig(
    override val version: Int = ConfigVersion.VERSION,
    val general: GeneralConfig = GeneralConfig(),
    val groups: List<ConfigGroup> = DefaultConfigs.GROUPS
) : VersionedConfig

@ConfigSerializable
data class GeneralConfig(
    val source: SourceType = SourceType.CONFIG,
    val defaultGroup: String = "default",
    val sync: SyncConfig = SyncConfig()
)

@ConfigSerializable
data class SyncConfig(
    val tablist: SyncTargets = SyncTargets(),
    val chat: SyncTargets = SyncTargets()
)

@ConfigSerializable
data class SyncTargets(
    val enabled: Boolean = true,
    val allServers: Boolean = true,
    val serverGroups: List<String> = emptyList(),
    val persistentServers: List<String> = emptyList()
)

enum class SourceType {
    CONFIG,
    LUCKPERMS
}

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