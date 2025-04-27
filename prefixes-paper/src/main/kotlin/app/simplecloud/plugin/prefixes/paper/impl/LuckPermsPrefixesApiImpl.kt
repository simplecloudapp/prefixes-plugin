package app.simplecloud.plugin.prefixes.paper.impl

import app.simplecloud.plugin.prefixes.api.LuckPermsGroup
import app.simplecloud.plugin.prefixes.api.PrefixesApi
import app.simplecloud.plugin.prefixes.api.PrefixesGroupIndexer
import net.luckperms.api.LuckPerms
import net.luckperms.api.event.node.NodeMutateEvent
import net.luckperms.api.event.user.track.UserPromoteEvent
import net.luckperms.api.model.group.Group
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import space.chunks.customname.api.CustomNameManager
import kotlin.jvm.optionals.getOrNull

class LuckPermsPrefixesApiImpl(
    private val luckPerms: LuckPerms,
    private val plugin: Plugin,
    name: CustomNameManager
) : PaperPrefixesApiImpl(plugin, name), PrefixesGroupIndexer<Player> {

    override fun indexGroups(api: PrefixesApi<Player>) {
        luckPerms.groupManager.loadAllGroups().newIncompleteFuture<Unit>().completeAsync {
            luckPerms.groupManager.loadedGroups.forEach {
                addGroup(LuckPermsGroup(it, luckPerms))
            }
        }
        luckPerms.eventBus.subscribe(plugin, UserPromoteEvent::class.java) { event ->
            val newGroup = event.groupTo.getOrNull() ?: return@subscribe
            val player = Bukkit.getPlayer(event.user.uniqueId) ?: return@subscribe
            if (!player.isOnline) return@subscribe
            setWholeName(player, newGroup)
        }
        luckPerms.eventBus.subscribe(plugin, NodeMutateEvent::class.java) { event ->
            if (!event.isGroup) return@subscribe
            val group = event.target as Group
            storedGroups.removeIf { it.getName() == group.name }
            val updated = LuckPermsGroup(group, luckPerms)
            addGroup(updated)
            Bukkit.getOnlinePlayers().forEach { if (getHighestGroup(it) == updated) setWholeName(it, updated) }
        }
    }
}