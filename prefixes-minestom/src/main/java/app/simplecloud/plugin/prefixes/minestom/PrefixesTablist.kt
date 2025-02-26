package app.simplecloud.plugin.prefixes.minestom

import app.simplecloud.plugin.prefixes.api.PrefixesDisplay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket
import net.minestom.server.network.packet.server.play.TeamsPacket
import net.minestom.server.scoreboard.Team

class PrefixesTablist : PrefixesDisplay<Component, Player, Team> {

    private val teams = mutableListOf<Team>()
    private val priorities = mutableMapOf<Team, Int>()
    private val viewers = mutableSetOf<Player>()
    private var color: TextColor = NamedTextColor.GRAY
    override fun addViewer(player: Player): Boolean {
        val result = viewers.add(player)
        if (result) {
            teams.forEach { team ->
                player.sendPacket(getCreateTeamPacket(team))
                team.players.forEach { teamPlayer ->
                    getUpdatePacket(teamPlayer)?.let { player.sendPacket(it) }
                }
            }
        }

        return result
    }

    override fun removeViewer(player: Player): Boolean {
        val result = viewers.remove(player)
        if (result) {
            teams.forEach { team ->
                player.sendPacket(getRemoveTeamPacket(team))
                team.players.forEach { teamPlayer ->
                    player.sendPacket(getDefaultPlayerInfoPacket(teamPlayer))
                }
            }
        }
        return result
    }

    override fun getViewers(): Set<Player> {
        return viewers
    }

    override fun createTeam(id: String, priority: Int): Team? {
        if (getTeam(id) != null) return null
        val team = MinecraftServer.getTeamManager().createTeam("${toPriorityString(priority)}$id")
        teams.add(team)
        return team
    }

    override fun getTeam(id: String): Team? {
        return teams.firstOrNull { it.teamName.endsWith(id) }
    }

    override fun getPriority(team: Team): Int? {
        return priorities[team]
    }

    fun copyTeam(team: Team, id: String, priority: Int): Team? {
        val newTeam = createTeam(id, priority) ?: return null
        newTeam.prefix = team.prefix
        newTeam.suffix = team.suffix
        newTeam.teamColor = team.teamColor
        newTeam.players.addAll(team.players)
        return newTeam
    }

    override fun updatePriority(id: String, priority: Int): Team? {
        val team = getTeam(id) ?: return null
        val newTeam = copyTeam(team, id, priority) ?: return null
        teams.remove(team)
        priorities.remove(team)
        teams.add(newTeam)
        priorities[newTeam] = priority
        return newTeam
    }

    override fun updateColor(id: String, color: TextColor) {
        val team = getTeam(id) ?: return
        team.teamColor = NamedTextColor.nearestTo(color)
        this.color = color
        render(team)
    }

    override fun setViewer(player: Player): Boolean {
        viewers.forEach { removeViewer(it) }
        return addViewer(player)
    }

    override fun removePlayer(player: Player) {
        teams.forEach {
            if (it.players.contains(player)) {
                it.removeMember(player.username)
            }
        }
    }

    override fun setPlayer(id: String, player: Player) {
        val team = getTeam(id) ?: createTeam(id) ?: return
        team.addMember(player.username)
    }

    override fun update(id: String, prefix: Component, suffix: Component, priority: Int) {
        val team = updatePriority(id, priority) ?: createTeam(id, priority) ?: return
        team.prefix = prefix
        team.suffix = suffix
        render(team)
    }

    override fun updateSuffix(id: String, suffix: Component) {
        val team = getTeam(id) ?: return
        team.suffix = suffix
        render(team)
    }

    override fun updatePrefix(id: String, prefix: Component) {
        val team = getTeam(id) ?: return
        team.prefix = prefix
        render(team)
    }

    private fun getUpdatePacket(player: Player): PlayerInfoUpdatePacket? {
        val team = teams.firstOrNull { it.players.contains(player) } ?: return null
        return PlayerInfoUpdatePacket(
            PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, PlayerInfoUpdatePacket.Entry(
                player.uuid,
                player.username,
                listOf(),
                true,
                player.latency,
                player.gameMode,
                team.prefix.append(Component.text(player.username).color(color)).append(team.suffix),
                null
            )
        )
    }

    private fun getDefaultPlayerInfoPacket(player: Player): PlayerInfoUpdatePacket {
        return PlayerInfoUpdatePacket(
            PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, PlayerInfoUpdatePacket.Entry(
                player.uuid,
                player.username,
                listOf(),
                true,
                player.latency,
                player.gameMode,
                player.displayName,
                null
            )
        )
    }

    private fun render(team: Team) {
        viewers.forEach { viewer ->
            team.players.forEach { player ->
                getUpdatePacket(player)?.let { viewer.sendPacket(it) }
            }
            viewer.sendPacket(getUpdateTeamPacket(team))
        }
    }

    private fun getUpdateTeamPacket(team: Team): TeamsPacket {
        return TeamsPacket(
            team.teamName, TeamsPacket.UpdateTeamAction(
                team.teamDisplayName,
                team.friendlyFlags,
                team.nameTagVisibility,
                team.collisionRule,
                team.teamColor,
                team.prefix,
                team.suffix
            )
        )
    }

    private fun getCreateTeamPacket(team: Team): TeamsPacket {
        return TeamsPacket(
            team.teamName, TeamsPacket.CreateTeamAction(
                team.teamDisplayName,
                team.friendlyFlags,
                team.nameTagVisibility,
                team.collisionRule,
                team.teamColor,
                team.prefix,
                team.suffix,
                team.members
            )
        )
    }

    private fun getRemoveTeamPacket(team: Team): TeamsPacket {
        return TeamsPacket(team.teamName, TeamsPacket.RemoveTeamAction())
    }
}