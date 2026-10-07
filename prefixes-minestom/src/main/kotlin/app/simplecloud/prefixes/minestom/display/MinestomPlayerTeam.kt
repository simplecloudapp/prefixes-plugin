package app.simplecloud.prefixes.minestom.display

import app.simplecloud.prefixes.shared.utilities.PriorityFormatter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.color.TeamColor
import net.minestom.server.network.packet.server.play.TeamsPacket
import net.minestom.server.network.packet.server.play.TeamsPacket.CollisionRule
import net.minestom.server.network.packet.server.play.TeamsPacket.NameTagVisibility

object MinestomPlayerTeam {

    fun register(name: String, priority: Int) {
        val manager = MinecraftServer.getTeamManager()
        val teamName = createName(name, priority)

        manager.deleteTeam(teamName)

        val team = manager
            .createBuilder(teamName)
            .prefix(Component.empty())
            .suffix(Component.empty())
            .teamColor(toTeamColor(NamedTextColor.WHITE))
            .nameTagVisibility(NameTagVisibility.ALWAYS)
            .build()

        team.addMember(name)
    }

    fun unregister(name: String, priority: Int) {
        MinecraftServer.getTeamManager().deleteTeam(createName(name, priority))
    }

    fun createPacket(
        name: String,
        priority: Int,
        prefix: Component = Component.empty(),
        suffix: Component = Component.empty(),
        color: TextColor = NamedTextColor.WHITE,
        hideNameTag: Boolean
    ): TeamsPacket {
        val settings = TeamsPacket.Settings(
            Component.empty(),
            prefix,
            suffix,
            if (hideNameTag) NameTagVisibility.NEVER else NameTagVisibility.ALWAYS,
            CollisionRule.ALWAYS,
            toTeamColor(color),
            0
        )

        return TeamsPacket(createName(name, priority), TeamsPacket.CreateTeamAction(settings, listOf(name)))
    }

    fun removePacket(packet: TeamsPacket): TeamsPacket = TeamsPacket(packet.teamName(), TeamsPacket.RemoveTeamAction())

    private fun createName(name: String, priority: Int): String = "${PriorityFormatter.format(priority)}_$name"

    private fun toTeamColor(color: TextColor): TeamColor {
        return TeamColor.valueOf(NamedTextColor.nearestTo(color).toString().uppercase())
    }
}
