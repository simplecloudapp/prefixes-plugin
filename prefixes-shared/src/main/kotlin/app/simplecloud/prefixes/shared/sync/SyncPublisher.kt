package app.simplecloud.prefixes.shared.sync

import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.prefixes.shared.config.PrefixesConfig
import app.simplecloud.prefixes.shared.config.SyncTargets
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntryMapper
import app.simplecloud.prefixes.shared.utilities.ComponentSerializer
import app.simplecloud.prefixes.shared.utilities.PrefixesSubjects
import app.simplecloud.prefixes.v1.ChatMessageEvent
import app.simplecloud.prefixes.v1.TablistEntryRemoveEvent
import app.simplecloud.prefixes.v1.TablistSyncRequest
import com.google.protobuf.MessageLite
import io.nats.client.Connection
import net.kyori.adventure.text.Component
import java.util.UUID

class SyncPublisher(
    private val connection: Connection,
    private val subjects: PrefixesSubjects,
    private val config: ConfigurationFactory<PrefixesConfig>
) {

    fun publishChatMessage(message: Component) = publish(sync().chat, PrefixesSubjects.CHAT) {
        ChatMessageEvent.newBuilder()
            .setJson(ComponentSerializer.serialize(message))
            .build()
    }

    fun publishTablistEntry(entry: TablistEntry) = publish(sync().tablist, PrefixesSubjects.TABLIST_UPDATE) {
        TablistEntryMapper.toDefinition(entry)
    }

    fun publishTablistRemove(uniqueId: UUID) = publish(sync().tablist, PrefixesSubjects.TABLIST_REMOVE) {
        TablistEntryRemoveEvent.newBuilder()
            .setPlayerId(uniqueId.toString())
            .build()
    }

    fun publishTablistRequest() = publish(sync().tablist, PrefixesSubjects.TABLIST_REQUEST) {
        TablistSyncRequest.getDefaultInstance()
    }

    private fun sync() = config.get().general.sync

    private fun publish(targets: SyncTargets, subject: String, message: () -> MessageLite) {
        if (!targets.enabled) return
        connection.publish(subjects.own(subject), message().toByteArray())
    }
}
