package app.simplecloud.prefixes.shared.platform

import java.util.UUID

/**
 * Represents a listener for prefixes events on a platform.
 */
interface PrefixesListener {

    /**
     * Called after the config and messages were reloaded.
     */
    fun onReload()

    /**
     * Called when prefixes shuts down.
     */
    fun onShutdown()

    /**
     * Called when the group of a player has to be reloaded.
     */
    fun onPlayerUpdate(id: UUID)

    /**
     * Called when manually set values of a player changed.
     */
    fun onPlayerDataChange(id: UUID)

    /**
     * Called when the groups of all players have to be reloaded.
     */
    fun onAllPlayersUpdate()
}
