package app.simplecloud.plugin.prefixes.paper.event

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * This event is called when a player was successfully registered in the [app.simplecloud.plugin.prefixes.api.PrefixesActor].
 * If you want to create custom logic (e.g. a custom suffix visible to the friends of this player)
 * you have to do it when listening on this event to make sure it works correctly.
 */
data class PrefixesConfiguredEvent(
    val player: Player
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