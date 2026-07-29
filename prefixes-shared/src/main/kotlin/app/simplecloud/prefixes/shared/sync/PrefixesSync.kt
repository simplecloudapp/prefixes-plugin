package app.simplecloud.prefixes.shared.sync

import app.simplecloud.api.CloudApi
import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.utilities.PrefixesSubjects
import io.nats.client.Connection
import io.nats.client.Nats
import io.nats.client.Options

class PrefixesSync(config: ConfigurationFactory<PrefixesConfig>) {

    private val api = CloudApi.create()
    private val connection = createNatsConnection()
    private val subjects = PrefixesSubjects(api.networkId)

    val publisher = SyncPublisher(connection, subjects, config)
    val subscriber = SyncSubscriber(connection, subjects, config)

    fun shutdown() {
        subscriber.close()
        connection.close()
        api.close()
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
