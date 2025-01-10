package app.simplecloud.plugin.prefixes.paper

import app.simplecloud.plugin.prefixes.api.PrefixesDisplay
import app.simplecloud.plugin.prefixes.paper.packet.PacketTeam
import app.simplecloud.plugin.prefixes.paper.packet.UpdateTeamMode
import app.simplecloud.plugin.prefixes.paper.packet.UpdateTeamPlayersMode
import com.comphenix.protocol.ProtocolManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player

class PaperPrefixesDisplay(
    private val manager: ProtocolManager,
) : PrefixesDisplay<Component, Player, PacketTeam> {

    private val teams: MutableMap<String, PacketTeam> = mutableMapOf()
    private val viewers: MutableSet<Player> = mutableSetOf()

    override fun createTeam(id: String, priority: Int): PacketTeam? {
        if (getTeam(id) != null) return null
        val name = "${toPriorityString(priority)}$id"
        val team = PacketTeam.create(name, null, null, null, priority, mutableListOf())
        teams[id] = team
        return team
    }

    override fun getTeam(id: String): PacketTeam? {
        return teams.getOrDefault(id, null)
    }

    override fun updatePriority(id: String, priority: Int): PacketTeam? {
        val team = getTeam(id) ?: return null
        val newId = "${toPriorityString(priority)}$id"
        val packets = team.getUpdateIdPackets(newId)
        team.priority = priority
        viewers.forEach { viewer ->
            packets.forEach { packet ->
                manager.sendServerPacket(viewer, packet)
            }
        }
        return team
    }

    override fun updateColor(id: String, color: TextColor) {
        val team = getTeam(id) ?: return
        team.color = color
        val packet = team.getTeamUpdatePacket(UpdateTeamMode.UPDATE)
        viewers.forEach { viewer ->
            manager.sendServerPacket(viewer, packet)
        }
    }

    override fun getViewers(): Set<Player> {
        return viewers
    }

    override fun removeViewer(player: Player): Boolean {
        val result = viewers.remove(player)
        if (result) {
            teams.values.forEach { team ->
                manager.sendServerPacket(player, team.getTeamDeletePacket())
            }
        }
        return result
    }

    override fun addViewer(player: Player): Boolean {
        val result = viewers.add(player)
        if (result) {
            teams.values.forEach { team ->
                team.getUpdateDisplayNamePackets().forEach { packet ->
                    manager.sendServerPacket(player, packet)
                }
            }
        }
        return result
    }

    override fun setViewer(player: Player): Boolean {
        viewers.forEach { removeViewer(it) }
        return addViewer(player)
    }

    override fun removePlayer(player: Player) {
        teams.forEach {
            if (it.value.members.contains(player)) {
                it.value.members.remove(player)
                val packet = it.value.getModifyTeamMembersPacket(UpdateTeamPlayersMode.REMOVE, listOf(player))
                val displayPacket = it.value.getUpdateDisplayNamePacket(player)
                viewers.forEach { viewer ->
                    manager.sendServerPacket(viewer, packet)
                    manager.sendServerPacket(viewer, displayPacket)
                }
            }
        }
    }

    override fun addPlayer(id: String, player: Player) {
        val team = getTeam(id) ?: return
        if (!team.members.contains(player)) {
            team.members.add(player)
            val packet = team.getModifyTeamMembersPacket(UpdateTeamPlayersMode.ADD, listOf(player))
            val displayPacket = team.getUpdateDisplayNamePacket(player)
            viewers.forEach { viewer ->
                manager.sendServerPacket(viewer, packet)
                manager.sendServerPacket(viewer, displayPacket)
            }
        }
    }

    override fun update(id: String, prefix: Component, suffix: Component, priority: Int) {
        val exists = getTeam(id) != null
        val team = updatePriority(id, priority) ?: createTeam(id, priority) ?: return
        team.prefix = prefix
        team.suffix = suffix
        val packet = team.getTeamUpdatePacket(if (exists) UpdateTeamMode.UPDATE else UpdateTeamMode.CREATE)
        val displayPackets = team.getUpdateDisplayNamePackets()
        viewers.forEach { viewer ->
            manager.sendServerPacket(viewer, packet)
            displayPackets.forEach { display ->
                manager.sendServerPacket(viewer, display)
            }
        }
    }

    override fun updateSuffix(id: String, suffix: Component) {
        val team = getTeam(id) ?: return
        team.suffix = suffix
        val packet = team.getTeamUpdatePacket(UpdateTeamMode.UPDATE)
        val displayPackets = team.getUpdateDisplayNamePackets()
        viewers.forEach { viewer ->
            manager.sendServerPacket(viewer, packet)
            displayPackets.forEach { display ->
                manager.sendServerPacket(viewer, display)
            }
        }
    }

    override fun updatePrefix(id: String, prefix: Component) {
        val team = getTeam(id) ?: return
        team.prefix = prefix
        val packet = team.getTeamUpdatePacket(UpdateTeamMode.UPDATE)
        val displayPackets = team.getUpdateDisplayNamePackets()
        viewers.forEach { viewer ->
            manager.sendServerPacket(viewer, packet)
            displayPackets.forEach { display ->
                manager.sendServerPacket(viewer, display)
            }
        }
    }
}