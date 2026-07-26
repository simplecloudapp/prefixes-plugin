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
    val defaultGroup: String = "default"
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