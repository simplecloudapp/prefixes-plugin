package app.simplecloud.prefixes.minestom.display

import app.simplecloud.prefixes.shared.sync.tablist.SourcedTablistEntry
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket.Action
import net.minestom.server.utils.PacketSendingUtils
import java.util.EnumSet
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MinestomTablist {

    private val entries = ConcurrentHashMap<UUID, SourcedTablistEntry>()

    fun update(publisherId: String, entry: TablistEntry) {
        if (isOnline(entry.uniqueId)) return

        val previous = entries.put(entry.uniqueId, SourcedTablistEntry(publisherId, entry))?.entry
        if (previous != entry) {
            PacketSendingUtils.broadcastPlayPacket(createInfoPacket(listOf(entry)))
        }
        if (previous == null || previous.name != entry.name || previous.priority != entry.priority) {
            if (previous != null) MinestomPlayerTeam.unregister(previous.name, previous.priority)
            MinestomPlayerTeam.register(entry.name, entry.priority)
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

    fun sync(player: Player) {
        val visible = entries.values.map(SourcedTablistEntry::entry).filter { entry -> !isOnline(entry.uniqueId) }
        visible.chunked(PlayerInfoUpdatePacket.MAX_ENTRIES).forEach { batch -> player.sendPacket(createInfoPacket(batch)) }
    }

    private fun hide(entry: TablistEntry) {
        MinestomPlayerTeam.unregister(entry.name, entry.priority)
        if (!isOnline(entry.uniqueId)) {
            PacketSendingUtils.broadcastPlayPacket(PlayerInfoRemovePacket(entry.uniqueId))
        }
    }

    private fun getUpdateActions(): EnumSet<Action> = EnumSet.of(
        Action.ADD_PLAYER,
        Action.UPDATE_LISTED,
        Action.UPDATE_GAME_MODE,
        Action.UPDATE_LATENCY,
        Action.UPDATE_DISPLAY_NAME,
        Action.UPDATE_LIST_ORDER,
        Action.UPDATE_HAT
    )

    private fun createInfoPacket(tablistEntries: Collection<TablistEntry>): PlayerInfoUpdatePacket {
        val packetEntries = tablistEntries.map { entry ->
            PlayerInfoUpdatePacket.Entry(
                entry.uniqueId,
                entry.name,
                entry.profileProperties.map { property -> PlayerInfoUpdatePacket.Property(property.name, property.value, property.signature) },
                true,
                entry.latency,
                GameMode.valueOf(entry.gameMode.name),
                entry.displayName,
                null,
                entry.listOrder,
                entry.showHat
            )
        }

        return PlayerInfoUpdatePacket(getUpdateActions(), packetEntries)
    }

    private fun isOnline(id: UUID): Boolean = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id) != null
}
