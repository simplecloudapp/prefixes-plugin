package app.simplecloud.prefixes.minestom.display

import app.simplecloud.prefixes.shared.sync.tablist.SyncedTablist
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.ServerPacket
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket.Action
import net.minestom.server.network.packet.server.play.TeamsPacket
import net.minestom.server.utils.PacketSendingUtils
import java.util.EnumSet
import java.util.UUID

class MinestomTablist : SyncedTablist<Player, ServerPacket>() {

    override fun isOnline(id: UUID): Boolean = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id) != null

    override fun createInfoPacket(entries: List<TablistEntry>): ServerPacket {
        val packetEntries = entries.map { entry ->
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

    override fun createInfoRemovePacket(id: UUID): ServerPacket = PlayerInfoRemovePacket(id)

    override fun createTeamPacket(entry: TablistEntry): ServerPacket = createTeam(entry)

    override fun createTeamRemovePacket(entry: TablistEntry): ServerPacket = MinestomPlayerTeam.removePacket(createTeam(entry))

    override fun send(player: Player, packet: ServerPacket) = player.sendPacket(packet)

    override fun broadcast(packet: ServerPacket) = PacketSendingUtils.broadcastPlayPacket(packet)

    private fun createTeam(entry: TablistEntry): TeamsPacket = MinestomPlayerTeam.createPacket(entry.name, entry.priority, hideNameTag = true)

    private fun getUpdateActions(): EnumSet<Action> = EnumSet.of(
        Action.ADD_PLAYER,
        Action.UPDATE_LISTED,
        Action.UPDATE_GAME_MODE,
        Action.UPDATE_LATENCY,
        Action.UPDATE_DISPLAY_NAME,
        Action.UPDATE_LIST_ORDER,
        Action.UPDATE_HAT
    )
}
