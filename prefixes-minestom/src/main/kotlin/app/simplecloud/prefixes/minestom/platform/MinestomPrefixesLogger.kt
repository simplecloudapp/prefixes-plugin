package app.simplecloud.prefixes.minestom.platform

import app.simplecloud.prefixes.shared.platform.PrefixesLogger
import org.slf4j.LoggerFactory

class MinestomPrefixesLogger : PrefixesLogger {

    private val logger = LoggerFactory.getLogger("simplecloud-prefixes")

    override fun info(msg: String) {
        logger.info(msg)
    }

    override fun warn(msg: String) {
        logger.warn(msg)
    }

    override fun error(msg: String, cause: Throwable) {
        logger.error(msg, cause)
    }

}