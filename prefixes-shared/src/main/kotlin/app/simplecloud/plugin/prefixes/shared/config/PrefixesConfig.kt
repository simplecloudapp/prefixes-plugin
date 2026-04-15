package app.simplecloud.plugin.prefixes.shared.config

import app.simplecloud.plugin.prefixes.shared.utilities.ConfigVersion

data class PrefixesConfig(
    val version: Char = ConfigVersion.VERSION
)
