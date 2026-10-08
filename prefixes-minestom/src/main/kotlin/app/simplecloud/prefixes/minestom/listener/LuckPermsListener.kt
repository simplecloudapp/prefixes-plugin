package app.simplecloud.prefixes.minestom.listener

import app.simplecloud.prefixes.minestom.display.MinestomDisplayManager
import net.luckperms.api.LuckPerms
import net.luckperms.api.event.node.NodeMutateEvent
import net.luckperms.api.event.user.UserDataRecalculateEvent
import net.luckperms.api.model.group.Group
import net.luckperms.api.model.user.User
import net.minestom.server.MinecraftServer
import java.util.UUID

class LuckPermsListener(
    private val luckPerms: LuckPerms,
    private val manager: MinestomDisplayManager
) {

    fun register() {
        luckPerms.eventBus.subscribe(UserDataRecalculateEvent::class.java) { event ->
            updatePlayer(event.user.uniqueId)
        }

        luckPerms.eventBus.subscribe(NodeMutateEvent::class.java) { event ->
            when (val target = event.target) {
                is User -> updatePlayer(target.uniqueId)
                is Group -> updateAllPlayers()
            }
        }
    }

    private fun updatePlayer(id: UUID) {
        val player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(id) ?: return
        manager.updatePlayer(player)
    }

    private fun updateAllPlayers() {
        MinecraftServer.getConnectionManager().onlinePlayers.forEach { player ->
            manager.updatePlayer(player)
        }
    }
}
