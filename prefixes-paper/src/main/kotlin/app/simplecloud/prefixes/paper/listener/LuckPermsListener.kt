package app.simplecloud.prefixes.paper.listener

import app.simplecloud.prefixes.paper.display.PaperDisplayManager
import net.luckperms.api.LuckPerms
import net.luckperms.api.event.node.NodeMutateEvent
import net.luckperms.api.event.user.UserDataRecalculateEvent
import net.luckperms.api.model.user.User
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class LuckPermsListener(
    private val plugin: Plugin,
    private val luckPerms: LuckPerms,
    private val manager: PaperDisplayManager
) {

    fun register() {
        luckPerms.eventBus.subscribe(plugin, UserDataRecalculateEvent::class.java) { event ->
            Bukkit.getScheduler().runTask(plugin, Runnable {
                val player = Bukkit.getPlayer(event.user.uniqueId) ?: return@Runnable
                manager.updatePlayer(player)
            })
        }

        luckPerms.eventBus.subscribe(plugin, NodeMutateEvent::class.java) { event ->
            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (event.isUser) {
                    val user = event.target as? User ?: return@Runnable
                    val player = Bukkit.getPlayer(user.uniqueId) ?: return@Runnable
                    manager.updatePlayer(player)
                } else if (event.isGroup) {
                    Bukkit.getOnlinePlayers().forEach { player ->
                        manager.updatePlayer(player)
                    }
                }
            })
        }
    }
}