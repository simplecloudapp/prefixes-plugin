package app.simplecloud.prefixes.shared

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.api.PrefixesApiProvider
import app.simplecloud.prefixes.shared.api.PrefixesApiImpl
import app.simplecloud.prefixes.shared.config.DefaultConfigInstaller
import app.simplecloud.prefixes.shared.config.MessageConfig
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.group.GroupProviderRegistry
import app.simplecloud.prefixes.shared.group.config.ConfigGroupProvider
import app.simplecloud.prefixes.shared.group.luckperms.LuckPermsGroupProvider
import app.simplecloud.prefixes.shared.platform.PrefixesListener
import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
import app.simplecloud.prefixes.shared.sync.PrefixesSync
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

class Prefixes(private val platform: PrefixesPlatform) {

    private val logger = platform.getLogger()
    private val listeners = CopyOnWriteArrayList<PrefixesListener>()

    val config = createConfig()
    val messages = createMessages()

    val registry = createGroupRegistry()
    val api = PrefixesApiImpl(registry, listeners, platform)
    val sync = createSync()

    fun startup() {
        PrefixesApiProvider.register(api)
    }

    fun shutdown() {
        PrefixesApiProvider.unregister()
        sync?.shutdown()
    }

    fun addListener(listener: PrefixesListener) {
        listeners.add(listener)
    }

    fun reload(): Boolean {
        return runCatching {
            logger.info("Reloading simplecloud prefixes...")
            config.reload()
            messages.reload()
            listeners.forEach(PrefixesListener::onReload)
        }.onSuccess {
            logger.info("Succesfully reloaded simplecloud prefixes")
        }.onFailure { throwable ->
            logger.error("Failed to reload simplecloud prefixes", throwable)
        }.isSuccess
    }

    fun getPlatform(): PrefixesPlatform {
        return platform
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
        val registry = GroupProviderRegistry(config, ConfigGroupProvider(logger, config, platform.getPermissionChecker()), platform)

        val luckPerms = platform.getLuckPerms()
        if (luckPerms != null) {
            registry.register(LuckPermsGroupProvider(logger, luckPerms))
        }

        return registry
    }

    private fun createSync(): PrefixesSync? {
        if (!isSyncEnabled()) return null

        if (!isSimpleCloudAvailable()) {
            logger.warn("Sync is enabled in the config, but SimpleCloud was not found on this server.")
            return null
        }

        return runCatching {
            PrefixesSync(config, logger)
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

    private fun isSyncEnabled(): Boolean {
        val current = config.get()
        if (!current.sync.enabled) return false

        return current.features.chat && current.sync.channels.chat || current.features.tablist && current.sync.channels.tablist
    }
}
