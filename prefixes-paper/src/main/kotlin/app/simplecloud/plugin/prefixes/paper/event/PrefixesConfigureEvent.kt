package app.simplecloud.plugin.prefixes.paper.event

import app.simplecloud.plugin.prefixes.api.PrefixesPlayerData
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * This event is called when a player is ready to receive his prefix.
 * You can modify each property, which will result in differences on the first render in tab.
 */
data class PrefixesConfigureEvent(
    val player: Player,
    val data: PrefixesPlayerData,
) : Event() {
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