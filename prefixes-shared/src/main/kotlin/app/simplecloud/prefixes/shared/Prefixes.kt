package app.simplecloud.prefixes.shared

import org.apache.logging.log4j.LogManager

class Prefixes {

    private val logger = LogManager.getLogger(Prefixes::class.java)

    fun startup() {
        logger.info("Starting up prefixes...")
    }

    fun shutdown() {
        logger.info("Shutdown prefixes...")
    }

}