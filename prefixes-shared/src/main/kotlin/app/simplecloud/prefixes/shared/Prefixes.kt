package app.simplecloud.prefixes.shared

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.api.PrefixesApi
import app.simplecloud.prefixes.api.PrefixesApiProvider
import app.simplecloud.prefixes.shared.api.PrefixesApiImpl
import app.simplecloud.prefixes.shared.config.DefaultConfigInstaller
import app.simplecloud.prefixes.shared.config.MessageConfig
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.group.GroupProviders
import app.simplecloud.prefixes.shared.group.config.ConfigGroupProvider
import app.simplecloud.prefixes.shared.group.luckperms.LuckPermsGroupProvider
import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
import app.simplecloud.prefixes.shared.platform.PrefixesReloadListener
import app.simplecloud.prefixes.shared.sync.PrefixesSync
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

class Prefixes(private val platform: PrefixesPlatform) {

    val config = createConfig()
    val messages = ConfigurationFactory(File(platform.getDataDirectory(), "messages.yml"), MessageConfig::class.java)

    init {
        config.loadOrCreate(PrefixesConfig())
        messages.loadOrCreate(MessageConfig())
    }

    private val listeners = CopyOnWriteArrayList<PrefixesReloadListener>()

    val api: PrefixesApi = PrefixesApiImpl(createGroupProviders(), platform)
    val sync = createSync()

    fun startup() {
        PrefixesApiProvider.register(api)
    }

    fun shutdown() {
        PrefixesApiProvider.unregister()
        sync?.shutdown()
    }

    fun addReloadListener(listener: PrefixesReloadListener) {
        listeners.add(listener)
    }

    fun reload() {
        config.reload()
        messages.reload()
        listeners.forEach(PrefixesReloadListener::onReload)
    }

    private fun createConfig(): ConfigurationFactory<PrefixesConfig> {
        val file = File(platform.getDataDirectory(), "config.yml")
        DefaultConfigInstaller.install(file.toPath(), javaClass.classLoader)
        return ConfigurationFactory(file, PrefixesConfig::class.java)
    }

    private fun createGroupProviders(): GroupProviders {
        return GroupProviders(
            config,
            ConfigGroupProvider(config, platform.getPermissionChecker()),
            createLuckPermsProvider()
        )
    }

    private fun createLuckPermsProvider(): LuckPermsGroupProvider? {
        val luckPerms = platform.getLuckPerms() ?: return null
        return LuckPermsGroupProvider(luckPerms)
    }

    private fun createSync(): PrefixesSync? {
        if (!isSyncEnabled()) return null
        return PrefixesSync(config)
    }

    private fun isSyncEnabled(): Boolean {
        val current = config.get()
        if (!current.sync.enabled) return false

        return current.features.chat && current.sync.channels.chat || current.features.tablist && current.sync.channels.tablist
    }
}
