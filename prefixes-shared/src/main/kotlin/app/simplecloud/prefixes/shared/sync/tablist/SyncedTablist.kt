package app.simplecloud.prefixes.shared.sync.tablist

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

abstract class SyncedTablist<P, K> {

    private val entries = ConcurrentHashMap<UUID, SourcedTablistEntry>()

    fun update(publisherId: String, entry: TablistEntry) {
        if (isOnline(entry.uniqueId)) return

        val previous = entries.put(entry.uniqueId, SourcedTablistEntry(publisherId, entry, TimeSource.Monotonic.markNow()))?.entry
        if (previous != entry) {
            broadcast(createInfoPacket(listOf(entry)))
        }
        if (previous == null || previous.name != entry.name || previous.priority != entry.priority) {
            if (previous != null) broadcast(createTeamRemovePacket(previous))
            broadcast(createTeamPacket(entry))
        }
    }

    fun remove(id: UUID) {
        val sourced = entries.remove(id) ?: return
        hide(sourced.entry)
    }

    fun remove(publisherId: String, id: UUID) {
        val sourced = entries[id] ?: return
        if (sourced.publisherId != publisherId) return
        if (entries.remove(id, sourced)) hide(sourced.entry)
    }

    fun clear() {
        entries.keys.toList().forEach { id -> remove(id) }
    }

    fun expire() {
        entries.entries
            .filter { (_, sourced) -> sourced.updatedAt.elapsedNow() > 90.seconds }
            .forEach { (id, sourced) -> if (entries.remove(id, sourced)) hide(sourced.entry) }
    }

    fun sync(player: P) {
        val visible = entries.values.map(SourcedTablistEntry::entry).filter { entry -> !isOnline(entry.uniqueId) }

        visible.chunked(100).forEach { batch -> send(player, createInfoPacket(batch)) }
        visible.forEach { entry -> send(player, createTeamPacket(entry)) }
    }

    protected abstract fun isOnline(id: UUID): Boolean

    protected abstract fun createInfoPacket(entries: List<TablistEntry>): K

    protected abstract fun createInfoRemovePacket(id: UUID): K

    protected abstract fun createTeamPacket(entry: TablistEntry): K

    protected abstract fun createTeamRemovePacket(entry: TablistEntry): K

    protected abstract fun send(player: P, packet: K)

    protected abstract fun broadcast(packet: K)

    private fun hide(entry: TablistEntry) {
        broadcast(createTeamRemovePacket(entry))
        if (!isOnline(entry.uniqueId)) {
            broadcast(createInfoRemovePacket(entry.uniqueId))
        }
    }

}