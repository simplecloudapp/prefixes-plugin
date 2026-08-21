package app.simplecloud.prefixes.shared.group

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.config.SourceType

class GroupProviders(
    private val config: ConfigurationFactory<PrefixesConfig>,
    private val configProvider: GroupProvider,
    private val luckPermsProvider: GroupProvider?
) {

    fun current(): GroupProvider = when (config.get().general.source) {
        SourceType.LUCKPERMS -> luckPermsProvider ?: configProvider
        SourceType.CONFIG -> configProvider
    }
}
