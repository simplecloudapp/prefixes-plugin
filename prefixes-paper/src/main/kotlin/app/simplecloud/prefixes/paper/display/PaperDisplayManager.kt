package app.simplecloud.prefixes.paper.display

import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import app.simplecloud.prefixes.shared.utilities.getTablistName
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import space.chunks.customname.api.CustomNameManager
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PaperDisplayManager(
    private val plugin: Plugin,
    private val prefixes: Prefixes,
    private val customNameManager: CustomNameManager
) {
    private val cache = ConcurrentHashMap<UUID, PrefixesPlayerData>()

    fun getPlayer(id: UUID): PrefixesPlayerData? {
        return cache[id]
    }

    fun updatePlayer(player: Player) {
        prefixes.api.getPrefixData(player.uniqueId).thenAccept { data ->
            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (!player.isOnline) return@Runnable
                cache[player.uniqueId] = data

                val tablistName = data.getTablistName()
                player.playerListName(tablistName)

                val team = PaperPlayerTeam(player.name, data.priority, data.prefix, data.suffix, data.color)
                broadcast(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true))

                val name = customNameManager.forEntity(player)
                name.setName(data.displayName)

                publish(player, data)
            })
        }.exceptionally { _ ->
            plugin.logger.warning("Failed to update prefix data of ${player.name}")
            null
        }
    }

    fun removePlayer(player: Player) {
        val data = cache.remove(player.uniqueId) ?: return
        val team = PaperPlayerTeam(player.name, data.priority, data.prefix, data.suffix, data.color)

        broadcast(ClientboundSetPlayerTeamPacket.createRemovePacket(team))
        prefixes.sync?.publisher?.publishTablistRemove(player.uniqueId)
    }

    fun syncPlayers(player: Player) {
        val connection = (player as CraftPlayer).handle.connection

        Bukkit.getOnlinePlayers().forEach { player ->
            val data = cache[player.uniqueId] ?: return@forEach
            val team = PaperPlayerTeam(player.name, data.priority, data.prefix, data.suffix, data.color)
            connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true))
        }
    }

    fun sync() {
        Bukkit.getOnlinePlayers().forEach { player ->
            val data = cache[player.uniqueId] ?: return@forEach
            publish(player, data)
        }
    }

    private fun publish(player: Player, data: PrefixesPlayerData) {
        prefixes.sync?.publisher?.publishTablistEntry(
            TablistEntry(player.uniqueId, player.name, data.getTablistName(), data.priority)
        )
    }

    private fun broadcast(packet: ClientboundSetPlayerTeamPacket) {
        Bukkit.getOnlinePlayers().forEach { player ->
            (player as CraftPlayer).handle.connection.send(packet)
        }
    }

}
