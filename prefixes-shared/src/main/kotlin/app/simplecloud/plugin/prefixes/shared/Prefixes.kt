package app.simplecloud.plugin.prefixes.shared

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.plugin.prefixes.shared.config.MessageConfig
import app.simplecloud.plugin.prefixes.shared.config.PrefixesConfig
import org.apache.logging.log4j.LogManager
import java.io.File

class Prefixes(
    dir: String
) {
    private val logger = LogManager.getLogger(Prefixes::class.java)

    private val config = ConfigurationFactory(File(dir, "config.yml"), PrefixesConfig::class.java)
    private val messageConfig = ConfigurationFactory(File(dir, "messages.yml"), MessageConfig::class.java)

    init {
        File(dir).mkdirs()
        config.loadOrCreate(PrefixesConfig())
        messageConfig.loadOrCreate(MessageConfig())
    }

    fun startup() {
        logger.info("Starting up prefixes...")
    }

    fun shutdown() {
        logger.info("Shutting down prefixes...")
    }

    fun reload() {
        config.loadOrCreate(PrefixesConfig())
        messageConfig.loadOrCreate(MessageConfig())
    }

}