package app.simplecloud.prefixes.shared.group

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.PrefixesConstants
import java.util.concurrent.ConcurrentHashMap

class GroupProviderRegistry(
    private val config: ConfigurationFactory<PrefixesConfig>,
    private val provider: GroupProvider
) {

    private val logger = PrefixesConstants.LOGGER
    private val providers = ConcurrentHashMap<String, GroupProvider>()

    init {
        register(provider)
    }

    fun register(provider: GroupProvider) {
        val key = provider.getName().trim().lowercase()
        providers[key] = provider
    }

    fun unregister(name: String) {
        providers.remove(name.trim().lowercase())
    }

    fun getAllGroupProviders(): Collection<GroupProvider> = providers.values.toList()

    fun getCurrentGroupProvider(): GroupProvider = providers[getSource()] ?: provider

    fun validateSource() {
        val source = getSource()
        if (providers.containsKey(source)) {
            logger.info("Using the group source '$source'")
            return
        }

        logger.warn("No group provider named '$source' is registered yet, using '${provider.getName()}' until it is registered")
    }

    private fun getSource(): String = config.get().general.source.trim().lowercase()
}
