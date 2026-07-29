package app.simplecloud.prefixes.shared

import app.simplecloud.prefixes.api.PrefixesApi
import app.simplecloud.prefixes.api.PrefixesApiProvider
import app.simplecloud.prefixes.shared.api.PrefixesApiImpl
import app.simplecloud.prefixes.shared.config.DefaultConfigInstaller
import app.simplecloud.prefixes.shared.config.MessageConfig
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.config.SourceType
import app.simplecloud.prefixes.shared.group.config.ConfigGroupProvider
import app.simplecloud.prefixes.shared.group.luckperms.LuckPermsGroupProvider
import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
import app.simplecloud.prefixes.shared.sync.PrefixesSync
import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import java.io.File

class Prefixes(private val platform: PrefixesPlatform) {

    private val configFile = File(platform.dataDirectory, "config.yml").also { file ->
        DefaultConfigInstaller.install(file.toPath(), javaClass.classLoader)
    }
    val config = ConfigurationFactory(
        configFile,
        PrefixesConfig::class.java
    )
    val messages = ConfigurationFactory(File(platform.dataDirectory, "messages.yml"), MessageConfig::class.java)

    init {
        config.loadOrCreate(PrefixesConfig())
        messages.loadOrCreate(MessageConfig())
    }

    private val configGroupProvider = ConfigGroupProvider(config, platform.permissionChecker)
    private val lpGroupProvider = platform.luckPerms?.let { LuckPermsGroupProvider(it) }

    val api = createApi()
    val sync = createSync()

    fun startup() {
        PrefixesApiProvider.register(api)
    }

    fun shutdown() {
        PrefixesApiProvider.unregister()
        sync?.shutdown()
    }

    fun reload() {
        config.reload()
        messages.reload()
        onReload?.invoke()
    }

    var onReload: (() -> Unit)? = null

    private fun createApi(): PrefixesApi = PrefixesApiImpl({
        val source = config.get().general.source
        if (source == SourceType.LUCKPERMS && lpGroupProvider != null) {
            lpGroupProvider
        } else {
            configGroupProvider
        }},
        platform.playerResolver
    )

    private fun createSync(): PrefixesSync? = config.get()
        .takeIf { config ->
            config.sync.enabled &&
                (
                    config.features.chat && config.sync.channels.chat ||
                        config.features.tablist && config.sync.channels.tablist
                )
        }
        ?.let { PrefixesSync(config) }

}
