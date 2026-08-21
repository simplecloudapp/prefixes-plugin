package app.simplecloud.prefixes.shared.config

import app.simplecloud.plugin.api.shared.config.VersionedConfig
import app.simplecloud.prefixes.shared.utilities.config.ConfigVersion
import app.simplecloud.prefixes.shared.utilities.config.DefaultConfigs
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class PrefixesConfig(
    override val version: Int = ConfigVersion.VERSION,
    val general: GeneralConfig = GeneralConfig(),
    val features: FeaturesConfig = FeaturesConfig(),
    val sync: SyncConfig = SyncConfig(),
    val groups: List<ConfigGroup> = DefaultConfigs.GROUPS
) : VersionedConfig

@ConfigSerializable
data class GeneralConfig(
    val source: String = CONFIG_SOURCE,
    val defaultGroup: String = "default"
)

@ConfigSerializable
data class FeaturesConfig(
    val chat: Boolean = true,
    val tablist: Boolean = true,
    val displayName: Boolean = true
)

@ConfigSerializable
data class SyncConfig(
    val enabled: Boolean = true,
    val channels: SyncChannels = SyncChannels(),
    val sources: List<String> = listOf(CURRENT_SYNC_SOURCE)
)

@ConfigSerializable
data class SyncChannels(
    val chat: Boolean = true,
    val tablist: Boolean = true
)

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

const val CONFIG_SOURCE = "config"
const val LUCKPERMS_SOURCE = "luckperms"

const val CURRENT_SYNC_SOURCE = "CURRENT"
const val ALL_SYNC_SOURCE = "ALL"
