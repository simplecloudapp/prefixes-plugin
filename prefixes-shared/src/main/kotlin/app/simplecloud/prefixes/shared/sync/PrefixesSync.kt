package app.simplecloud.prefixes.shared.sync

import app.simplecloud.api.CloudApi
import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.platform.PrefixesLogger
import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options

class PrefixesSync(
    config: ConfigurationFactory<PrefixesConfig>,
    private val logger: PrefixesLogger
) {

    private val api = CloudApi.create()
    private val connection = createNatsConnection()
    private val subjects = PrefixesSubjects(api.networkId)

    val publisher = SyncPublisher(connection, subjects, config, logger)
    val subscriber = SyncSubscriber(connection, subjects, config, logger)

    fun shutdown() {
        runCatching {
            logger.info("Shutdown prefixes sync...")
            subscriber.close()
            connection.close()
            api.close()
        }.onFailure { throwable ->
            logger.error("Failed to shutdown sync", throwable)
        }.onSuccess {
            logger.info("Successfully shutdown sync")
        }
    }

    private fun createNatsConnection(): Connection {
        return Nats.connect(
            Options.builder()
                .server(System.getenv().getOrDefault("SIMPLECLOUD_NATS_URL", "wss://nats.simplecloud.app:443"))
                .userInfo(System.getenv().getOrDefault("SIMPLECLOUD_NETWORK_ID", "default"), System.getenv().getOrDefault("SIMPLECLOUD_NETWORK_SECRET", ""))
                .maxReconnects(-1)
                .build()
        )
    }
}
