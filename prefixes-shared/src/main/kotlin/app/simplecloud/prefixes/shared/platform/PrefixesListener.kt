package app.simplecloud.prefixes.shared.platform

import java.util.UUID

/**
 * The listener that listen to update events.
 */
interface PrefixesListener {

    /**
     * Called when the plugin reloads.
     */
    fun onReload()

    /**
     * Called when a player was update or requested by the api.
     */
    fun onPlayerUpdate(id: UUID)

    /**
     * Called when all players on this server updated or by the api.
     */
    fun onAllPlayersUpdate()
}
