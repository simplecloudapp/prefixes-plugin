package app.simplecloud.prefixes.shared.config

import app.simplecloud.plugin.api.shared.config.VersionedConfig
import app.simplecloud.prefixes.shared.PrefixesConstants
import app.simplecloud.prefixes.shared.utilities.config.ConfigVersion
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class PrefixesConfig(
    override val version: Int = ConfigVersion.VERSION,
    val general: GeneralConfig = GeneralConfig(),
    val features: FeaturesConfig = FeaturesConfig(),
    val sync: SyncConfig = SyncConfig(),
    val groups: List<ConfigGroup> = emptyList()
) : VersionedConfig {

    fun isChatSynced(): Boolean = features.chat && sync.enabled && sync.channels.chat

    fun isTablistSynced(): Boolean = features.tablist && sync.enabled && sync.channels.tablist
}

@ConfigSerializable
data class GeneralConfig(
    val source: String = PrefixesConstants.CONFIG_SOURCE,
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
    val sources: List<String> = listOf(PrefixesConstants.CURRENT_SYNC_SOURCE)
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