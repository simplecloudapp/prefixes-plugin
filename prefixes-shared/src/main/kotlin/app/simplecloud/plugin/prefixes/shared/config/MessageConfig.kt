package app.simplecloud.plugin.prefixes.shared.config

import app.simplecloud.plugin.prefixes.shared.utilities.ConfigVersion

data class MessageConfig(
    val version: Char = ConfigVersion.VERSION
)
