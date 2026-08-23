package app.simplecloud.prefixes.minestom.display

import app.simplecloud.prefixes.shared.sync.tablist.ProfileProperty
import app.simplecloud.prefixes.shared.sync.tablist.SourcedTablistEntry
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket
import net.minestom.server.scoreboard.Team
import net.minestom.server.utils.PacketSendingUtils
import java.util.EnumSet
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MinestomTablist {

    private val entries = ConcurrentHashMap<UUID, SourcedTablistEntry>()
    private val teams = ConcurrentHashMap<UUID, Team>()

    fun update(publisherId: String, entry: TablistEntry) {
        if (isOnline(entry.uniqueId)) return

        val previous = entries.put(entry.uniqueId, SourcedTablistEntry(publisherId, entry))?.entry
        val actions = getActions(entry, previous)

        if (actions.isNotEmpty()) {
            PacketSendingUtils.broadcastPlayPacket(createInfoPacket(listOf(entry), actions))
        }
        if (previous == null || previous.name != entry.name || previous.priority != entry.priority) {
            updateTeam(entry)
        }
    }

    fun remove(id: UUID) {
        removeEntry(null, id)
    }

    fun remove(publisherId: String, id: UUID) {
        removeEntry(publisherId, id)
    }

    fun clear() {
        entries.keys.toList().forEach { id -> remove(id) }
    }

    fun sync(player: Player) {
        val visibleEntries = entries.values
            .map(SourcedTablistEntry::entry)
            .filter { !isOnline(it.uniqueId) }

        visibleEntries.chunked(PlayerInfoUpdatePacket.MAX_ENTRIES).forEach { batch ->
            player.sendPacket(createInfoPacket(batch, getFullUpdateActions()))
        }
    }

    private fun removeEntry(publisherId: String?, id: UUID) {
        val sourcedEntry = entries[id] ?: return
        if (publisherId != null && sourcedEntry.publisherId != publisherId) return
        if (!entries.remove(id, sourcedEntry)) return

        removeTeam(id)
        if (isOnline(id)) return

        PacketSendingUtils.broadcastPlayPacket(PlayerInfoRemovePacket(id))
    }

    private fun updateTeam(entry: TablistEntry) {
        removeTeam(entry.uniqueId)
        teams[entry.uniqueId] = MinestomPlayerTeam.create(entry.name, entry.priority)
    }

    private fun removeTeam(id: UUID) {
        val team = teams.remove(id) ?: return
        MinestomPlayerTeam.delete(team)
    }

    private fun createInfoPacket(
        tablistEntries: Collection<TablistEntry>,
        actions: EnumSet<PlayerInfoUpdatePacket.Action>
    ): PlayerInfoUpdatePacket {
        val packetEntries = tablistEntries.map { entry ->
            PlayerInfoUpdatePacket.Entry(
                entry.uniqueId,
                entry.name,
                entry.profileProperties.map(::createProperty),
                true,
                entry.latency,
                GameMode.valueOf(entry.gameMode.name),
                entry.displayName,
                null,
                entry.listOrder,
                entry.showHat
            )
        }

        return PlayerInfoUpdatePacket(actions, packetEntries)
    }

    private fun getActions(
        entry: TablistEntry,
        previous: TablistEntry?
    ): EnumSet<PlayerInfoUpdatePacket.Action> {
        if (previous == null ||
            previous.name != entry.name ||
            previous.profileProperties != entry.profileProperties
        ) {
            return getFullUpdateActions()
        }

        val actions = EnumSet.noneOf(PlayerInfoUpdatePacket.Action::class.java)
        if (previous.gameMode != entry.gameMode) {
            actions.add(PlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE)
        }
        if (previous.latency != entry.latency) {
            actions.add(PlayerInfoUpdatePacket.Action.UPDATE_LATENCY)
        }
        if (previous.displayName != entry.displayName) {
            actions.add(PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME)
        }
        if (previous.listOrder != entry.listOrder) {
            actions.add(PlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER)
        }
        if (previous.showHat != entry.showHat) {
            actions.add(PlayerInfoUpdatePacket.Action.UPDATE_HAT)
        }
        return actions
    }

    private fun getFullUpdateActions(): EnumSet<PlayerInfoUpdatePacket.Action> =
        EnumSet.of(
            PlayerInfoUpdatePacket.Action.ADD_PLAYER,
            PlayerInfoUpdatePacket.Action.UPDATE_LISTED,
            PlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
            PlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
            PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
            PlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER,
            PlayerInfoUpdatePacket.Action.UPDATE_HAT
        )

    private fun createProperty(property: ProfileProperty): PlayerInfoUpdatePacket.Property {
        val signature = property.signature ?: return PlayerInfoUpdatePacket.Property(property.name, property.value)
        return PlayerInfoUpdatePacket.Property(property.name, property.value, signature)
    }

    private fun isOnline(id: UUID): Boolean {
        return MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id) != null
    }
}
