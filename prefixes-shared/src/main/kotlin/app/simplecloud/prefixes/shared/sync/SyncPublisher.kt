package app.simplecloud.prefixes.shared.sync

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.shared.config.FeaturesConfig
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.config.SyncChannels
import app.simplecloud.prefixes.shared.platform.PrefixesLogger
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntryMapper
import app.simplecloud.prefixes.shared.utilities.ComponentSerializer
import app.simplecloud.prefixes.v1.ChatMessageEvent
import app.simplecloud.prefixes.v1.TablistEntryRemoveEvent
import app.simplecloud.prefixes.v1.TablistSyncRequest
import com.google.protobuf.MessageLite
import io.nats.client.Connection
import net.kyori.adventure.text.Component
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class SyncPublisher(
    private val connection: Connection,
    private val subjects: PrefixesSubjects,
    private val config: ConfigurationFactory<PrefixesConfig>,
    private val logger: PrefixesLogger
) {

    fun publishChatMessage(message: Component) = publish(
        FeaturesConfig::chat,
        SyncChannels::chat,
        PrefixesSubjects.CHAT
    ) {
        ChatMessageEvent.newBuilder()
            .setJson(ComponentSerializer.serialize(message))
            .build()
    }

    fun publishTablistEntry(entry: TablistEntry) = publish(
        FeaturesConfig::tablist,
        SyncChannels::tablist,
        PrefixesSubjects.TABLIST_UPDATE
    ) {
        TablistEntryMapper.toDefinition(entry)
    }

    fun publishTablistRemove(uniqueId: UUID) = publish(
        FeaturesConfig::tablist,
        SyncChannels::tablist,
        PrefixesSubjects.TABLIST_REMOVE
    ) {
        TablistEntryRemoveEvent.newBuilder()
            .setPlayerId(uniqueId.toString())
            .build()
    }

    fun publishTablistRequest() = publish(
        FeaturesConfig::tablist,
        SyncChannels::tablist,
        PrefixesSubjects.TABLIST_REQUEST
    ) {
        TablistSyncRequest.getDefaultInstance()
    }

    private fun publish(
        feature: (FeaturesConfig) -> Boolean,
        channel: (SyncChannels) -> Boolean,
        subject: String,
        message: () -> MessageLite
    ) {
        val config = config.get()
        if (!feature(config.features) || !config.sync.enabled || !channel(config.sync.channels)) return

        val target = subjects.own(subject)

        try {
            connection.publish(target, message().toByteArray())
        } catch (e: Exception) {
            logger.error("Failed to publish sync message on '$subject'", e)
        }
    }

}
