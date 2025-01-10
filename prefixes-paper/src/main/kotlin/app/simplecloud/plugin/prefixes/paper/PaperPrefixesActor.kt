package app.simplecloud.plugin.prefixes.paper

import app.simplecloud.plugin.prefixes.api.PrefixesActor
import app.simplecloud.plugin.prefixes.api.PrefixesApi
import app.simplecloud.plugin.prefixes.api.PrefixesGroup
import app.simplecloud.plugin.prefixes.shared.MiniMessageImpl
import app.simplecloud.plugin.prefixes.paper.packet.PacketTeam
import com.comphenix.protocol.ProtocolManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class PaperPrefixesActor(
    private var scoreboard: PaperPrefixesGlobalDisplay,
    private val manager: ProtocolManager,
) : PrefixesActor {

    init {
        scoreboard.setDefaultDisplay(PaperPrefixesDisplay(manager))
    }

    override fun registerViewer(target: UUID, api: PrefixesApi) {
        val targetPlayer = Bukkit.getPlayer(target) ?: return
        val display = PaperPrefixesDisplay(manager)
        scoreboard.register(target, display)
        display.setViewer(targetPlayer)
        val defaultDisplay = scoreboard.getDefaultDisplay() ?: return
        Bukkit.getOnlinePlayers().forEach { player ->
            if (player.uniqueId != target) {
                val team = defaultDisplay.getTeam(player.name) ?: return@forEach
                apply(
                    player.uniqueId,
                    team.prefix ?: Component.text(""),
                    team.color ?: NamedTextColor.WHITE,
                    team.suffix ?: Component.text(""),
                    team.priority ?: 0,
                    target
                )
            }
        }
    }

    override fun hasViewer(target: UUID): Boolean {
        return scoreboard.getDisplay(target).orElse(null) != null
    }

    override fun removeViewer(target: UUID) {
        val player = Bukkit.getPlayer(target) ?: return
        val display = scoreboard.getDisplay(target).orElse(null) ?: return
        display.removeViewer(player)
        scoreboard.removeDisplay(target)
    }

    override fun applyGroup(
        target: UUID,
        group: PrefixesGroup,
        vararg viewers: UUID
    ) {
        val player: Player = Bukkit.getPlayer(target) ?: return
        scoreboard.update(
            player.name,
            group.getPrefix() ?: Component.text(""),
            group.getSuffix() ?: Component.text(""),
            group.getPriority(),
            *viewers
        )
        if (group.getColor() != null)
            setColor(target, group.getColor()!!, *viewers)
        scoreboard.removePlayer(player, *viewers)
        scoreboard.addPlayer(player.name, player, *viewers)
        apply(
            target,
            group.getPrefix() ?: Component.text(""),
            group.getColor() ?: NamedTextColor.WHITE,
            group.getSuffix() ?: Component.text(""),
            group.getPriority(),
            *viewers
        )
    }

    override fun remove(target: UUID) {
        val player: Player = Bukkit.getPlayer(target) ?: return
        scoreboard.removePlayer(player)
    }

    override fun setPrefix(target: UUID, prefix: Component, vararg viewers: UUID) {
        val player = Bukkit.getPlayer(target) ?: return
        scoreboard.updatePrefix(player.name, prefix, *viewers)
    }

    override fun setSuffix(target: UUID, suffix: Component, vararg viewers: UUID) {
        val player = Bukkit.getPlayer(target) ?: return
        scoreboard.updateSuffix(player.name, suffix, *viewers)
    }

    override fun formatMessage(target: UUID, viewer: UUID?, format: String, message: Component): Component {
        val display = if (viewer != null) scoreboard.getDisplay(viewer)
            .orElseGet { scoreboard.getDefaultDisplay() } else scoreboard.getDefaultDisplay() ?: return message
        val targetPlayer = Bukkit.getPlayer(target) ?: return message
        val team: PacketTeam? = display.getTeam(targetPlayer.name)
        val tags = mutableListOf<TagResolver>()
        if (team != null) {
            tags.add(Placeholder.component("prefix", team.prefix ?: Component.text("")))
            tags.add(Placeholder.component("suffix", team.suffix ?: Component.text("")))
            tags.add(
                Placeholder.component(
                    "name_colored",
                    Component.text(Bukkit.getPlayer(target)!!.name).color(team.color)
                )
            )
            tags.add(Placeholder.unparsed("name", targetPlayer.name))
        } else {
            tags.add(Placeholder.unparsed("name", targetPlayer.name))
        }
        tags.add(Placeholder.component("message", message))
        return MiniMessageImpl.parse(format, tags)
    }

    override fun setColor(target: UUID, color: TextColor, vararg viewers: UUID) {
        val player = Bukkit.getPlayer(target) ?: return
        scoreboard.updateColor(player.name, color, *viewers)
    }

    override fun apply(
        target: UUID,
        prefix: Component,
        color: TextColor,
        suffix: Component,
        priority: Int,
        vararg viewers: UUID
    ) {
        val player: Player = Bukkit.getPlayer(target) ?: return
        scoreboard.update(
            player.name,
            prefix,
            suffix,
            priority,
            *viewers
        )
        setColor(target, color, *viewers)
        scoreboard.removePlayer(player, *viewers)
        scoreboard.addPlayer(player.name, player, *viewers)
    }
}