package app.simplecloud.prefixes.paper.listener

import app.simplecloud.prefixes.paper.display.PaperDisplayManager
import net.luckperms.api.LuckPerms
import net.luckperms.api.event.node.NodeMutateEvent
import net.luckperms.api.event.user.UserDataRecalculateEvent
import net.luckperms.api.model.group.Group
import net.luckperms.api.model.user.User
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.UUID

class LuckPermsListener(
    private val plugin: Plugin,
    private val luckPerms: LuckPerms,
    private val manager: PaperDisplayManager
) {

    fun register() {
        luckPerms.eventBus.subscribe(plugin, UserDataRecalculateEvent::class.java) { event ->
            val id = event.user.uniqueId
            Bukkit.getScheduler().runTask(plugin, Runnable { updatePlayer(id) })
        }

        luckPerms.eventBus.subscribe(plugin, NodeMutateEvent::class.java) { event ->
            val target = event.target
            Bukkit.getScheduler().runTask(plugin, Runnable {
                when (target) {
                    is User -> updatePlayer(target.uniqueId)
                    is Group -> updateAllPlayers()
                }
            })
        }
    }

    private fun updatePlayer(id: UUID) {
        val player = Bukkit.getPlayer(id) ?: return
        manager.updatePlayer(player)
    }

    private fun updateAllPlayers() {
        Bukkit.getOnlinePlayers().forEach { player ->
            manager.updatePlayer(player)
        }
    }
}
