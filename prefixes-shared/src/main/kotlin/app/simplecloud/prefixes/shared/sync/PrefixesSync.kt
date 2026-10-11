package app.simplecloud.prefixes.shared.sync

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.PrefixesConstants
import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options
import java.time.Duration

class PrefixesSync(
    config: ConfigurationFactory<PrefixesConfig>
) {

    private val logger = PrefixesConstants.LOGGER
    private val connection = createNatsConnection()
    private val subjects = PrefixesSubjects()

    val publisher = SyncPublisher(connection, subjects, config)
    val subscriber = SyncSubscriber(connection, subjects, config)

    fun shutdown() {
        runCatching {
            logger.info("Shutdown prefixes sync...")
            connection.flush(Duration.ofSeconds(1))
            subscriber.close()
            connection.close()
        }.onFailure { throwable ->
            logger.error("Failed to shutdown sync", throwable)
        }.onSuccess {
            logger.info("Successfully shutdown sync")
        }
    }

    private fun createNatsConnection(): Connection {
        val nats = Nats.connect(
            Options.builder()
                .server(System.getenv().getOrDefault("SIMPLECLOUD_NATS_URL", "wss://nats.simplecloud.app:443"))
                .userInfo(System.getenv().getOrDefault("SIMPLECLOUD_NETWORK_ID", "default"), System.getenv().getOrDefault("SIMPLECLOUD_NETWORK_SECRET", ""))
                .maxReconnects(-1)
                .build()
        )

        logger.info("Connected to NATS server (${nats.connectedUrl})")
        return nats
    }
}
