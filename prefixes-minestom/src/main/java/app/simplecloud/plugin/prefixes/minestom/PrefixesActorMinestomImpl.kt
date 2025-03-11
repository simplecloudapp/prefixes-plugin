package app.simplecloud.plugin.prefixes.minestom

import app.simplecloud.plugin.prefixes.api.PrefixesActor
import app.simplecloud.plugin.prefixes.api.PrefixesApi
import app.simplecloud.plugin.prefixes.api.PrefixesGroup
import app.simplecloud.plugin.prefixes.shared.MiniMessageImpl
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import java.util.*

class PrefixesActorMinestomImpl(private var scoreboard: PrefixesGlobalDisplayMinestomImpl) : PrefixesActor {
    override fun registerViewer(target: UUID, api: PrefixesApi) {
        scoreboard.register(target, PrefixesTablist())
    }

    override fun hasViewer(target: UUID): Boolean {
        return scoreboard.getDisplay(target).orElse(null) != null
    }

    override fun removeViewer(target: UUID) {
        val player = MinecraftServer.getConnectionManager().getPlayer(target) ?: return
        val display = scoreboard.getDisplay(player.uuid).orElse(null) ?: return
        display.removeViewer(player)
        scoreboard.removeDisplay(player.uuid)
    }

    override fun applyGroup(
        target: UUID,
        group: PrefixesGroup,
        vararg viewers: UUID,
    ) {
        apply(target, group.getPrefix() ?: Component.text(""), group.getColor() ?: NamedTextColor.WHITE, group.getSuffix() ?: Component.text(""), group.getPriority(), *viewers)
    }

    override fun remove(target: UUID) {
        val player: Player = MinecraftServer.getConnectionManager().getPlayer(target) ?: return
        scoreboard.removePlayer(player)
    }

    override fun setPrefix(target: UUID, prefix: Component, vararg viewers: UUID) {
        val player: Player = MinecraftServer.getConnectionManager().getPlayer(target) ?: return
        scoreboard.updatePrefix(player.username, prefix)
    }

    override fun setSuffix(target: UUID, suffix: Component, vararg viewers: UUID) {
        val player: Player = MinecraftServer.getConnectionManager().getPlayer(target) ?: return
        scoreboard.updateSuffix(player.username, suffix)
    }

    override fun formatMessage(target: UUID, viewer: UUID?, format: String, message: Component): Component {
        val targetPlayer = MinecraftServer.getConnectionManager().getPlayer(target) ?: return message
        val display = if (viewer != null) scoreboard.getDisplay(viewer)
            .orElse(scoreboard.getDefaultDisplay()) else scoreboard.getDefaultDisplay() ?: return message
        val team = display.getTeam(targetPlayer.username)
        val tags = mutableListOf<TagResolver>()
        if (team != null) {
            tags.add(Placeholder.component("prefix", team.prefix))
            tags.add(Placeholder.component("suffix", team.suffix))
            tags.add(
                Placeholder.component(
                    "name_colored",
                    Component.text(MinecraftServer.getConnectionManager().getPlayer(target)!!.username)
                        .color(team.teamColor)
                )
            )
            tags.add(
                Placeholder.component(
                    "name",
                    Component.text(MinecraftServer.getConnectionManager().getPlayer(target)!!.username)
                )
            )
        } else {
            tags.add(Placeholder.unparsed("name", "%s"))
        }
        tags.add(Placeholder.component("message", message))
        return MiniMessageImpl.parse(format, tags)
    }

    override fun setColor(target: UUID, color: TextColor, vararg viewers: UUID) {
        val player: Player = MinecraftServer.getConnectionManager().getPlayer(target) ?: return
        scoreboard.updateColor(player.username, color, *viewers)
    }

    override fun apply(
        target: UUID,
        prefix: Component,
        color: TextColor,
        suffix: Component,
        priority: Int,
        vararg viewers: UUID
    ) {
        val player: Player = MinecraftServer.getConnectionManager().getPlayer(target) ?: return
        scoreboard.update(
            player.username,
            prefix, suffix, priority,
            *viewers
        )
        setColor(target, color, *viewers)
        scoreboard.removePlayer(player, *viewers)
        scoreboard.addPlayer(player.username, player, *viewers)
    }
}