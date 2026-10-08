package app.simplecloud.prefixes.shared

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object PrefixesConstants {

    const val CONFIG_SOURCE = "config"

    const val CURRENT_SYNC_SOURCE = "CURRENT"
    const val ALL_SYNC_SOURCE = "ALL"

    const val DEFAULT_DISPLAY_NAME = "<color><playername>"
    const val DEFAULT_CHAT_FORMAT = "<prefix><color><playername><suffix> <#475569>» <#F8FAFC><message>"

    val LOGGER: Logger = LoggerFactory.getLogger("simplecloud-prefixes")

    val SCOPE = CoroutineScope(
        CoroutineName("simplecloud-prefixes") +
                SupervisorJob() +
                Dispatchers.IO +
                CoroutineExceptionHandler { _, exception ->
                    LOGGER.error("An error occurred in a coroutine", exception)
                }
    )

}