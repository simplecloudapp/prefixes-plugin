package app.simplecloud.prefixes.shared

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.api.PrefixesApi
import app.simplecloud.prefixes.api.PrefixesApiProvider
import app.simplecloud.prefixes.shared.api.PrefixesApiImpl
import app.simplecloud.prefixes.shared.config.MessageConfig
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.config.SourceType
import app.simplecloud.prefixes.shared.group.config.ConfigGroupProvider
import app.simplecloud.prefixes.shared.group.luckperms.LuckPermsGroupProvider
import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
import java.io.File

class Prefixes(platform: PrefixesPlatform) {

    val config = ConfigurationFactory(File(platform.dataDirectory, "config.yml"), PrefixesConfig::class.java).apply {
        loadOrCreate(PrefixesConfig())
    }
    val messages = ConfigurationFactory(File(platform.dataDirectory, "messages.yml"), MessageConfig::class.java).apply {
        loadOrCreate(MessageConfig())
    }

    private val configGroupProvider = ConfigGroupProvider(config, platform.permissionChecker)
    private val lpGroupProvider = platform.luckPerms?.let { LuckPermsGroupProvider(it) }

    val api: PrefixesApi = PrefixesApiImpl({
        val source = config.get().general.source
        if (source == SourceType.LUCKPERMS && lpGroupProvider != null) {
            lpGroupProvider
        } else {
            configGroupProvider
        }
    },
        platform.playerResolver
    )

    fun startup() {
        PrefixesApiProvider.register(api)
    }

    fun shutdown() {
        PrefixesApiProvider.unregister()
    }

    fun reload() {
        config.reload()
        messages.reload()
        onReload?.invoke()
    }

    var onReload: (() -> Unit)? = null

}