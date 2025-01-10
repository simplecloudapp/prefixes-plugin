package app.simplecloud.plugin.prefixes.paper.event

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * This event is called when a player is ready to receive his prefix.
 * You can modify each property, which will result in differences on the first render in tab.
 */
data class PrefixesConfigureEvent(
    val player: Player,
    var prefix: Component?,
    var suffix: Component?,
    var color: TextColor?,
    var priority: Int,
): Event() {
    companion object {
        private val handlers: HandlerList = HandlerList()
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }
    override fun getHandlers(): HandlerList {
        return Companion.handlers
    }
}