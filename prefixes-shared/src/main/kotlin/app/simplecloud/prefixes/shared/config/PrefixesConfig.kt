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
