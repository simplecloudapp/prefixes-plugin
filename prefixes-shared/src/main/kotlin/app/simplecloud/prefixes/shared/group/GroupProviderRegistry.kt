package app.simplecloud.prefixes.shared.group

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
import java.util.concurrent.ConcurrentHashMap

class GroupProviderRegistry(
    private val config: ConfigurationFactory<PrefixesConfig>,
    private val provider: GroupProvider,
    private val platform: PrefixesPlatform
) {

    private val logger = platform.getLogger()
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

    fun getCurrentGroupProvider(): GroupProvider {
        val source = config.get().general.source.trim().lowercase()
        val provider = providers[source] ?: return warnMissingProvider(source)
        return provider
    }

    private fun warnMissingProvider(source: String): GroupProvider {
        logger.warn("No group provider named '$source' is registered, using '${provider.getName()}' instead")
        return provider
    }
}
