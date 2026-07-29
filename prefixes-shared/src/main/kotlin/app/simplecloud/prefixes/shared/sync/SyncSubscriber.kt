package app.simplecloud.prefixes.shared.sync

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.shared.config.FeaturesConfig
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.config.SyncChannels
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntryMapper
import app.simplecloud.prefixes.shared.utilities.ComponentSerializer
import app.simplecloud.prefixes.shared.utilities.PrefixesSubjects
import app.simplecloud.prefixes.v1.ChatMessageEvent
import app.simplecloud.prefixes.v1.TablistEntryRemoveEvent
import app.simplecloud.prefixes.v1.TablistEntryUpdateEvent
import io.nats.client.Connection
import io.nats.client.Message
import net.kyori.adventure.text.Component
import java.time.Duration
import java.util.UUID

class SyncSubscriber(
    connection: Connection,
    private val subjects: PrefixesSubjects,
    private val config: ConfigurationFactory<PrefixesConfig>
) {

    private val dispatcher = connection.createDispatcher(null)

    fun subscribeChatMessage(handler: (Component) -> Unit) {
        subscribe(FeaturesConfig::chat, SyncChannels::chat, PrefixesSubjects.CHAT) { message ->
            handler(ComponentSerializer.deserialize(ChatMessageEvent.parseFrom(message.data).json))
        }
    }

    fun subscribeTablist(
        onUpdate: (String, TablistEntry) -> Unit,
        onRemove: (String, UUID) -> Unit,
        onRequest: () -> Unit
    ) {
        subscribe(
            FeaturesConfig::tablist,
            SyncChannels::tablist,
            PrefixesSubjects.TABLIST_UPDATE
        ) { message ->
            onUpdate(
                subjects.publisherId(message.subject, PrefixesSubjects.TABLIST_UPDATE),
                TablistEntryMapper.fromDefinition(TablistEntryUpdateEvent.parseFrom(message.data))
            )
        }
        subscribe(
            FeaturesConfig::tablist,
            SyncChannels::tablist,
            PrefixesSubjects.TABLIST_REMOVE
        ) { message ->
            onRemove(
                subjects.publisherId(message.subject, PrefixesSubjects.TABLIST_REMOVE),
                UUID.fromString(TablistEntryRemoveEvent.parseFrom(message.data).playerId)
            )
        }
        subscribe(
            FeaturesConfig::tablist,
            SyncChannels::tablist,
            PrefixesSubjects.TABLIST_REQUEST
        ) {
            onRequest()
        }
    }

    fun close() {
        dispatcher.drain(Duration.ofSeconds(1))
    }

    private fun subscribe(
        feature: (FeaturesConfig) -> Boolean,
        channel: (SyncChannels) -> Boolean,
        subject: String,
        handler: (Message) -> Unit
    ) {
        val config = config.get()
        if (!feature(config.features) || !config.sync.enabled || !channel(config.sync.channels)) return

        subjects.patterns(config.sync.sources, subject).forEach { pattern ->
            dispatcher.subscribe(pattern) { message ->
                val currentConfig = this.config.get()
                if (feature(currentConfig.features) &&
                    currentConfig.sync.enabled &&
                    channel(currentConfig.sync.channels) &&
                    !subjects.isOwn(message.subject)
                ) {
                    handler(message)
                }
            }
        }
    }
}
