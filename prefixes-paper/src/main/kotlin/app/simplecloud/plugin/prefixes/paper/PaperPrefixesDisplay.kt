package app.simplecloud.plugin.prefixes.paper

import app.simplecloud.plugin.prefixes.api.PrefixesDisplay
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.world.level.GameType
import net.minecraft.world.scores.PlayerTeam
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

class PaperPrefixesDisplay : PrefixesDisplay<Component, Player, PaperPlayerTeam> {

    private val teams: MutableMap<String, PaperPlayerTeam> = mutableMapOf()
    private val players: MutableMap<String, String> = mutableMapOf()
    private val viewers: MutableSet<Player> = mutableSetOf()

    override fun createTeam(id: String, priority: Int): PaperPlayerTeam? {
        if (getTeam(id) != null) return null
        val team = PaperPlayerTeam(id, priority)
        teams[id] = team
        return team
    }

    override fun getTeam(id: String): PaperPlayerTeam? {
        return teams.getOrDefault(id, null)
    }

    override fun getPriority(team: PaperPlayerTeam): Int {
        return team.priority
    }

    override fun updatePriority(id: String, priority: Int): PaperPlayerTeam? {
        val team = getTeam(id) ?: return null
        val deletePacket = ClientboundSetPlayerTeamPacket.createRemovePacket(team)
        viewers.forEach { viewer ->
            (viewer as CraftPlayer).handle.connection.send(deletePacket)
        }
        val newTeam = changeTeamPriority(priority, team) ?: return null
        val createPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(newTeam, true)
        val addPlayersPacket = ClientboundSetPlayerTeamPacket.createMultiplePlayerPacket(
            newTeam,
            getPlayersForTeam(newTeam),
            ClientboundSetPlayerTeamPacket.Action.ADD
        )
        viewers.forEach { viewer ->
            with(viewer as CraftPlayer) {
                handle.connection.send(createPacket)
                handle.connection.send(addPlayersPacket)
            }
        }
        teams[id] = newTeam
        return team
    }

    override fun updateColor(id: String, color: TextColor) {
        val team = getTeam(id) ?: return
        team.realColor = color
        val updatePacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false)
        viewers.forEach { viewer ->
            (viewer as CraftPlayer).handle.connection.send(updatePacket)
        }
        sendUpdateDisplayNamePackets(team)
    }

    override fun getViewers(): Set<Player> {
        return viewers
    }

    override fun removeViewer(player: Player): Boolean {
        val result = viewers.remove(player)
        if (result) {
            teams.values.forEach { team ->
                val deletePacket = ClientboundSetPlayerTeamPacket.createRemovePacket(team)
                (player as CraftPlayer).handle.connection.send(deletePacket)
                getPlayersForTeam(team).filter { Bukkit.getPlayer(it)?.isOnline ?: false }
                    .map { Bukkit.getPlayer(it)!! }.forEach { sendUpdateDisplayNamePacket(it) }
            }
        }
        return result
    }

    override fun addViewer(player: Player): Boolean {
        val result = viewers.add(player)
        if (result) {
            teams.values.forEach { team ->
                val createPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false)
                val addPlayersPacket = ClientboundSetPlayerTeamPacket.createMultiplePlayerPacket(
                    team,
                    getPlayersForTeam(team),
                    ClientboundSetPlayerTeamPacket.Action.ADD
                )
                with(player as CraftPlayer) {
                    handle.connection.send(createPacket)
                    handle.connection.send(addPlayersPacket)
                }
                sendUpdateDisplayNamePackets(team)
            }
        }
        return result
    }

    override fun setViewer(player: Player): Boolean {
        viewers.forEach { removeViewer(it) }
        return addViewer(player)
    }

    override fun removePlayer(player: Player) {
        val playerTeam = players[player.name] ?: return
        val team = teams.filter { it.key == playerTeam }.map { it.value }.firstOrNull() ?: return
        players.remove(player.name)
        val packet = ClientboundSetPlayerTeamPacket.createPlayerPacket(
            team,
            player.name,
            ClientboundSetPlayerTeamPacket.Action.REMOVE
        )
        viewers.forEach { viewer -> (viewer as CraftPlayer).handle.connection.send(packet) }
        sendUpdateDisplayNamePackets(team)
    }

    override fun setPlayer(id: String, player: Player) {
        val team = teams[id] ?: return
        if (players.contains(player.name)) {
            teams[players[player.name]]?.let { existing ->
                val delete = ClientboundSetPlayerTeamPacket.createPlayerPacket(
                    existing,
                    player.name,
                    ClientboundSetPlayerTeamPacket.Action.REMOVE
                )
                viewers.forEach { viewer -> (viewer as CraftPlayer).handle.connection.send(delete) }
            }

        }
        players[player.name] = id
        val packet = ClientboundSetPlayerTeamPacket.createPlayerPacket(
            team,
            player.name,
            ClientboundSetPlayerTeamPacket.Action.ADD
        )
        viewers.forEach { viewer -> (viewer as CraftPlayer).handle.connection.send(packet) }
        sendUpdateDisplayNamePackets(team)
    }

    override fun update(id: String, prefix: Component, suffix: Component, priority: Int) {
        val exists = getTeam(id) != null
        val team = updatePriority(id, priority) ?: createTeam(id, priority) ?: return
        team.playerPrefix = PaperAdventure.asVanilla(prefix)
        team.playerSuffix = PaperAdventure.asVanilla(suffix)
        teams[id] = team
        val packet = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, !exists)

        viewers.forEach { viewer ->
            with(viewer as CraftPlayer) {
                handle.connection.send(packet)
            }
        }
        sendUpdateDisplayNamePackets(team)
    }

    override fun updateSuffix(id: String, suffix: Component) {
        val team = getTeam(id) ?: return
        team.playerSuffix = PaperAdventure.asVanilla(suffix)
        teams[id] = team
        val packet = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false)
        viewers.forEach { viewer ->
            with(viewer as CraftPlayer) {
                handle.connection.send(packet)
            }
        }
        sendUpdateDisplayNamePackets(team)
    }

    override fun updatePrefix(id: String, prefix: Component) {
        val team = getTeam(id) ?: return
        team.playerPrefix = PaperAdventure.asVanilla(prefix)
        teams[id] = team
        val packet = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false)
        viewers.forEach { viewer ->
            with(viewer as CraftPlayer) {
                handle.connection.send(packet)
            }
        }
        sendUpdateDisplayNamePackets(team)
    }

    private fun sendUpdateDisplayNamePackets(team: PaperPlayerTeam) {
        val id = teams.filter { it.value == team }.keys.firstOrNull() ?: return
        val players = players.filter { it.value == id && Bukkit.getPlayer(it.key)?.isOnline ?: false }.keys.map {
            Bukkit.getPlayer(it)!!
        }
        players.forEach { player -> sendUpdateDisplayNamePacket(player, team) }
    }

    private fun sendUpdateDisplayNamePacket(player: Player, passed: PaperPlayerTeam? = null) {
        val team = passed ?: teams[players[player.name]]
        val update = ClientboundPlayerInfoUpdatePacket(
            EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
            ClientboundPlayerInfoUpdatePacket.Entry(
                player.uniqueId,
                null,
                true,
                -1,
                GameType.ADVENTURE,
                PaperAdventure.asVanilla(team?.getFormattedName(player.name()) ?: player.name()),
                true,
                -1,
                null
            )
        )
        viewers.forEach { viewer ->
            with(viewer as CraftPlayer) {
                handle.connection.send(update)
            }
        }
    }

    private fun changeTeamPriority(priority: Int, team: PaperPlayerTeam): PaperPlayerTeam? {
        val id = teams.filter { it.value == team }.keys.firstOrNull() ?: return null
        val playersInTeam = getPlayersForTeam(team)
        val newTeam = PaperPlayerTeam(id, priority)
        playersInTeam.forEach { player -> players[player] = id }
        newTeam.playerPrefix = team.playerPrefix
        newTeam.displayName = team.displayName
        newTeam.playerSuffix = team.playerSuffix
        newTeam.nameTagVisibility = team.nameTagVisibility
        newTeam.setSeeFriendlyInvisibles(team.canSeeFriendlyInvisibles())
        newTeam.deathMessageVisibility = team.deathMessageVisibility
        newTeam.collisionRule = team.collisionRule
        newTeam.realColor = team.realColor
        return newTeam
    }

    private fun getPlayersForTeam(team: PlayerTeam): List<String> {
        val id = teams.filter { it.value == team }.keys.firstOrNull() ?: return emptyList()
        return players.filter { it.value == id }
            .map { it.key }
    }
}