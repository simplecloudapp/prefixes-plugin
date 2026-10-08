package app.simplecloud.prefixes.shared

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.api.PrefixesApiProvider
import app.simplecloud.prefixes.shared.api.PrefixesApiImpl
import app.simplecloud.prefixes.shared.config.DefaultConfigInstaller
import app.simplecloud.prefixes.shared.config.MessageConfig
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.config.SyncConfig
import app.simplecloud.prefixes.shared.data.PrefixesDataRegistry
import app.simplecloud.prefixes.shared.group.GroupProviderRegistry
import app.simplecloud.prefixes.shared.group.config.ConfigGroupProvider
import app.simplecloud.prefixes.shared.group.luckperms.LuckPermsGroupProvider
import app.simplecloud.prefixes.shared.platform.PrefixesListener
import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
import app.simplecloud.prefixes.shared.sync.PrefixesSync
import app.simplecloud.prefixes.shared.utilities.ColorParser
import kotlinx.coroutines.cancelChildren
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

class Prefixes(private val platform: PrefixesPlatform) {

    private val logger = PrefixesConstants.LOGGER
    private val listeners = CopyOnWriteArrayList<PrefixesListener>()

    val config = createConfig()
    val messages = createMessages()

    val registry = createGroupRegistry()
    val dataRegistry = PrefixesDataRegistry(platform)
    val api = PrefixesApiImpl(registry, dataRegistry, listeners, platform)
    val sync = createSync()

    fun startup() {
        PrefixesApiProvider.register(api)
        validateConfig()
    }

    fun shutdown() {
        listeners.forEach(PrefixesListener::onShutdown)
        PrefixesApiProvider.unregister()
        sync?.shutdown()
        PrefixesConstants.SCOPE.coroutineContext.cancelChildren()
    }

    fun addListener(listener: PrefixesListener) {
        listeners.add(listener)
    }

    fun reload(): Boolean {
        return runCatching {
            logger.info("Reloading simplecloud prefixes...")
            val previousSync = config.get().sync
            config.reload()
            messages.reload()
            validateConfig()
            warnIfRestartRequired(previousSync)
            listeners.forEach(PrefixesListener::onReload)
        }.onSuccess {
            logger.info("Succesfully reloaded simplecloud prefixes")
        }.onFailure { throwable ->
            logger.error("Failed to reload simplecloud prefixes", throwable)
        }.isSuccess
    }

    private fun validateConfig() {
        registry.validateSource()
        config.get().groups
            .filter { group -> group.color.isNotBlank() && ColorParser.parse(group.color) == null }
            .forEach { group -> logger.warn("The color '${group.color}' of the group '${group.name}' is invalid, using white instead") }
    }

    private fun warnIfRestartRequired(previous: SyncConfig) {
        val current = config.get().sync
        if ((sync == null && current.enabled) || previous.sources != current.sources) {
            logger.warn("Enabling sync or changing sync.sources only takes effect after a restart")
        }
    }

    private fun createConfig(): ConfigurationFactory<PrefixesConfig> {
        val file = File(platform.getDataDirectory(), "config.yml")
        DefaultConfigInstaller.install(file.toPath(), javaClass.classLoader)

        val factory = ConfigurationFactory(file, PrefixesConfig::class.java)
        factory.loadOrCreate(PrefixesConfig())
        return factory
    }

    private fun createMessages(): ConfigurationFactory<MessageConfig> {
        val factory = ConfigurationFactory(File(platform.getDataDirectory(), "messages.yml"), MessageConfig::class.java)
        factory.loadOrCreate(MessageConfig())
        return factory
    }

    private fun createGroupRegistry(): GroupProviderRegistry {
        val registry = GroupProviderRegistry(config, ConfigGroupProvider(config, platform.getPermissionChecker()))

        val luckPerms = platform.getLuckPerms()
        if (luckPerms != null) {
            registry.register(LuckPermsGroupProvider(luckPerms))
        }

        return registry
    }

    private fun createSync(): PrefixesSync? {
        if (!config.get().sync.enabled) return null

        if (!isSimpleCloudAvailable()) {
            logger.warn("Sync is enabled in the config, but SimpleCloud was not found on this server.")
            return null
        }

        return runCatching {
            PrefixesSync(config)
        }.onFailure { throwable ->
            logger.error("Failed to initialize sync", throwable)
        }.getOrNull()
    }

    private fun isSimpleCloudAvailable(): Boolean {
        return try {
            Class.forName("app.simplecloud.api.CloudApi")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }
}
