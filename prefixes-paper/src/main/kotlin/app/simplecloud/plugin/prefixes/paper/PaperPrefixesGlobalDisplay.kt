package app.simplecloud.plugin.prefixes.paper

import app.simplecloud.plugin.prefixes.api.PrefixesGlobalDisplay
import app.simplecloud.plugin.prefixes.paper.packet.PacketTeam
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import space.chunks.customname.api.CustomNameManager
import java.util.*

class PaperPrefixesGlobalDisplay(
    private val name: CustomNameManager
) : PrefixesGlobalDisplay<Component, Player, PacketTeam>() {

    override fun addPlayer(id: String, player: Player, vararg players: UUID) {
        super.addPlayer(id, player, *players)
        name.forEntity(player).setName { viewer ->
            val defaultDisplay = getDefaultDisplay() ?: return@setName Component.text(player.name)
            val display = getDisplay(viewer.uniqueId).orElse(null) ?: defaultDisplay
            val team = display.getTeam(player.name) ?: defaultDisplay.getTeam(player.name)
            ?: return@setName Component.text(player.name)
            return@setName team.asComponent(player)
        }
    }
}