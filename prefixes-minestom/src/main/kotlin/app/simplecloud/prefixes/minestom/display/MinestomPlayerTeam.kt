package app.simplecloud.prefixes.minestom.display

import app.simplecloud.prefixes.shared.utilities.PriorityFormatter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.color.TeamColor
import net.minestom.server.network.packet.server.play.TeamsPacket.NameTagVisibility
import net.minestom.server.scoreboard.Team

object MinestomPlayerTeam {

    fun create(
        name: String,
        priority: Int,
        prefix: Component = Component.empty(),
        suffix: Component = Component.empty(),
        color: TextColor = NamedTextColor.WHITE,
        hideNameTag: Boolean = false
    ): Team {
        val teamManager = MinecraftServer.getTeamManager()
        val teamName = "${PriorityFormatter.format(priority)}_$name"

        teamManager.deleteTeam(teamName)

        val team = teamManager
            .createBuilder(teamName)
            .prefix(prefix)
            .suffix(suffix)
            .teamColor(toTeamColor(color))
            .nameTagVisibility(if (hideNameTag) NameTagVisibility.NEVER else NameTagVisibility.ALWAYS)
            .build()

        team.addMember(name)
        return team
    }

    fun delete(team: Team) {
        MinecraftServer.getTeamManager().deleteTeam(team)
    }

    private fun toTeamColor(color: TextColor): TeamColor {
        return TeamColor.valueOf(NamedTextColor.nearestTo(color).toString().uppercase())
    }
}
