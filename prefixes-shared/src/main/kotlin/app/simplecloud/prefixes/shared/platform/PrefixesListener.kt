package app.simplecloud.prefixes.shared.platform

import java.util.UUID

interface PrefixesListener {

    fun onReload()

    fun onPlayerUpdate(id: UUID)

    fun onAllPlayersUpdate()
}
