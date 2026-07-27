package app.simplecloud.prefixes.shared.sync

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.config.SyncTargets
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntryMapper
import app.simplecloud.prefixes.shared.utilities.ComponentSerialization
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
        subscribe(sync().chat, PrefixesSubjects.CHAT) { message ->
            handler(ComponentSerialization.deserialize(ChatMessageEvent.parseFrom(message.data).json))
        }
    }

    fun subscribeTablist(
        onUpdate: (TablistEntry) -> Unit,
        onRemove: (UUID) -> Unit,
        onRequest: () -> Unit
    ) {
        subscribeTablist(
            onUpdate = { _, entry -> onUpdate(entry) },
            onRemove = { _, id -> onRemove(id) },
            onRequest = onRequest
        )
    }

    fun subscribeTablist(
        onUpdate: (String, TablistEntry) -> Unit,
        onRemove: (String, UUID) -> Unit,
        onRequest: () -> Unit
    ) {
        val targets = sync().tablist

        subscribe(targets, PrefixesSubjects.TABLIST_UPDATE) { message ->
            onUpdate(
                subjects.publisherId(message.subject, PrefixesSubjects.TABLIST_UPDATE),
                TablistEntryMapper.fromDefinition(TablistEntryUpdateEvent.parseFrom(message.data))
            )
        }
        subscribe(targets, PrefixesSubjects.TABLIST_REMOVE) { message ->
            onRemove(
                subjects.publisherId(message.subject, PrefixesSubjects.TABLIST_REMOVE),
                UUID.fromString(TablistEntryRemoveEvent.parseFrom(message.data).playerId)
            )
        }
        subscribe(targets, PrefixesSubjects.TABLIST_REQUEST) {
            onRequest()
        }
    }

    fun close() {
        dispatcher.drain(Duration.ofSeconds(1))
    }

    private fun sync() = config.get().general.sync

    private fun subscribe(targets: SyncTargets, subject: String, handler: (Message) -> Unit) {
        if (!targets.enabled) return

        subjects.patterns(targets, subject).forEach { pattern ->
            dispatcher.subscribe(pattern) { message ->
                if (!subjects.isOwn(message.subject)) handler(message)
            }
        }
    }
}
