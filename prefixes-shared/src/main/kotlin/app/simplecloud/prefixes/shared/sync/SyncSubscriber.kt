package app.simplecloud.prefixes.shared.sync

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntryMapper
import app.simplecloud.prefixes.shared.utilities.ComponentSerializer
import app.simplecloud.prefixes.shared.PrefixesConstants
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
    private val logger = PrefixesConstants.LOGGER
    private val dispatcher = connection.createDispatcher(null)

    fun subscribeChatMessage(handler: (Component) -> Unit) {
        subscribe(PrefixesConfig::isChatSynced, PrefixesSubjects.CHAT) { message ->
            handler(ComponentSerializer.deserialize(ChatMessageEvent.parseFrom(message.data).json))
        }
    }

    fun subscribeTablist(
        onUpdate: (String, TablistEntry) -> Unit,
        onRemove: (String, UUID) -> Unit,
        onRequest: () -> Unit
    ) {
        subscribe(PrefixesConfig::isTablistSynced, PrefixesSubjects.TABLIST_UPDATE) { message ->
            onUpdate(
                subjects.publisherId(message.subject, PrefixesSubjects.TABLIST_UPDATE),
                TablistEntryMapper.fromDefinition(TablistEntryUpdateEvent.parseFrom(message.data))
            )
        }
        subscribe(PrefixesConfig::isTablistSynced, PrefixesSubjects.TABLIST_REMOVE) { message ->
            onRemove(
                subjects.publisherId(message.subject, PrefixesSubjects.TABLIST_REMOVE),
                UUID.fromString(TablistEntryRemoveEvent.parseFrom(message.data).playerId)
            )
        }
        subscribe(PrefixesConfig::isTablistSynced, PrefixesSubjects.TABLIST_REQUEST) {
            onRequest()
        }
    }

    fun close() {
        try {
            dispatcher.drain(Duration.ofSeconds(1))
        } catch (e: Exception) {
            logger.error("Failed to drain prefixes sync dispatcher", e)
        }
    }

    private fun subscribe(enabled: (PrefixesConfig) -> Boolean, subject: String, handler: (Message) -> Unit) {
        subjects.patterns(config.get().sync.sources, subject).forEach { pattern ->
            runCatching { subscribe(pattern, enabled, handler) }.onFailure { throwable ->
                logger.error("Failed to subscribe to the sync subject '$pattern'", throwable)
            }
        }
    }

    private fun subscribe(pattern: String, enabled: (PrefixesConfig) -> Boolean, handler: (Message) -> Unit) {
        dispatcher.subscribe(pattern) { message ->
            if (!enabled(config.get()) || subjects.isOwn(message.subject)) return@subscribe

            try {
                handler(message)
            } catch (e: Exception) {
                logger.error("Failed to handle sync message on '${message.subject}'", e)
            }
        }
    }

}