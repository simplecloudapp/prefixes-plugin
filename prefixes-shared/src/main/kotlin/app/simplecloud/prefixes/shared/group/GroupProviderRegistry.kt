package app.simplecloud.prefixes.shared.group

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

class GroupProviderRegistry(
    private val config: ConfigurationFactory<PrefixesConfig>,
    private val provider: GroupProvider
) {

    private val logger = Logger.getLogger("simplecloud-prefixes")

    private val providers = ConcurrentHashMap<String, GroupProvider>()
    private val sources = ConcurrentHashMap.newKeySet<String>()

    init {
        register(provider)
    }

    fun register(provider: GroupProvider) {
        val key = toKey(provider.name)
        providers[key] = provider
        sources.remove(key)
    }

    fun unregister(name: String) {
        providers.remove(toKey(name))
    }

    fun getAllGroupProviders(): Collection<GroupProvider> = providers.values.toList()

    fun getCurrentGroupProvider(): GroupProvider {
        val source = toKey(config.get().general.source)
        val provider = providers[source] ?: return reportMissing(source)
        return provider
    }

    private fun reportMissing(source: String): GroupProvider {
        if (sources.add(source)) {
            logger.warning("No group provider named '$source' is registered, using '${provider.name}' instead")
        }

        return provider
    }

    private fun toKey(name: String): String = name.trim().lowercase()
}
