package app.simplecloud.prefixes.paper.platform

import app.simplecloud.prefixes.shared.platform.PrefixesLogger
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class PaperPrefixesLogger(private val plugin: Plugin) : PrefixesLogger {

    override fun info(msg: String) {
        return plugin.logger.info(msg)
    }

    override fun warn(msg: String) {
       return plugin.logger.warning(msg)
    }

    override fun error(msg: String, thrown: Throwable) {
        return plugin.logger.log(Level.SEVERE, msg, thrown)
    }

}