package app.simplecloud.prefixes.paper.display

import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.sync.tablist.ProfileProperty
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import app.simplecloud.prefixes.shared.sync.tablist.TablistGameMode
import app.simplecloud.prefixes.shared.utilities.PlayerDisplayFormatter
import com.destroystokyo.paper.ClientOption
import net.kyori.adventure.text.Component
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
    private val playerTeams = ConcurrentHashMap<UUID, PaperPlayerTeam>()
    private val publishedEntries = ConcurrentHashMap<UUID, TablistEntry>()

    fun getPlayer(id: UUID): PrefixesPlayerData? {
        return cache[id]
    }

    fun addPlayer(player: Player) {
        updatePlayer(player)
        Bukkit.getScheduler().runTask(plugin, Runnable {
            if (player.isOnline && cache[player.uniqueId] == null) {
                publish(player, Component.text(player.name), 0)
            }
        })
    }

    fun updatePlayer(player: Player) {
        prefixes.api.getPrefixData(player.uniqueId).thenAccept { data ->
            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (!player.isOnline) return@Runnable
                cache[player.uniqueId] = data

                val features = prefixes.config.get().features
                val displayName = PlayerDisplayFormatter.displayName(
                    data,
                    player.name,
                    features.displayName
                )

                if (features.tablist) {
                    player.playerListName(PlayerDisplayFormatter.formatTablistName(data, displayName))
                } else {
                    player.playerListName(null)
                }

                playerTeams.remove(player.uniqueId)?.let { previous ->
                    broadcast(ClientboundSetPlayerTeamPacket.createRemovePacket(previous))
                }

                val team = when {
                    features.tablist -> PaperPlayerTeam(
                        player.name,
                        data.priority,
                        data.prefix,
                        data.suffix,
                        data.color,
                        hideNameTag = features.displayName
                    )
                    features.displayName -> PaperPlayerTeam(player.name, priority = 0)
                    else -> null
                }
                team?.let {
                    playerTeams[player.uniqueId] = it
                    broadcast(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(it, true))
                }

                val name = customNameManager.forEntity(player)
                if (features.displayName) {
                    name.setName(displayName)
                    name.setHidden(false)
                } else {
                    name.setHidden(true)
                }

                publish(player, data, displayName)
            })
        }.exceptionally { _ ->
            plugin.logger.warning("Failed to update prefix data of ${player.name}")
            null
        }
    }

    fun removePlayer(player: Player) {
        cache.remove(player.uniqueId)
        playerTeams.remove(player.uniqueId)?.let { team ->
            broadcast(ClientboundSetPlayerTeamPacket.createRemovePacket(team))
        }

        publishedEntries.remove(player.uniqueId)
        prefixes.sync?.publisher?.publishTablistRemove(player.uniqueId)
    }

    fun syncPlayers(player: Player) {
        val connection = (player as CraftPlayer).handle.connection

        playerTeams.values.forEach { team ->
            connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true))
        }
    }

    fun sync(force: Boolean = false) {
        Bukkit.getOnlinePlayers().forEach { player ->
            val data = cache[player.uniqueId] ?: return@forEach
            val features = prefixes.config.get().features
            val displayName = PlayerDisplayFormatter.displayName(
                data,
                player.name,
                features.displayName
            )
            publish(player, data, displayName, force)
        }
    }

    private fun publish(
        player: Player,
        data: PrefixesPlayerData,
        displayName: Component,
        force: Boolean = false
    ) {
        publish(player, PlayerDisplayFormatter.formatTablistName(data, displayName), data.priority, force)
    }

    private fun publish(player: Player, displayName: Component, priority: Int, force: Boolean = false) {
        val config = prefixes.config.get()
        if (!config.features.tablist || !config.sync.enabled || !config.sync.channels.tablist) return
        val publisher = prefixes.sync?.publisher ?: return
        val profileProperties = player.playerProfile.properties.map { property ->
            ProfileProperty(property.name, property.value, property.signature)
        }

        val entry = TablistEntry(
            uniqueId = player.uniqueId,
            name = player.name,
            displayName = displayName,
            priority = priority,
            profileProperties = profileProperties,
            latency = player.ping,
            gameMode = TablistGameMode.valueOf(player.gameMode.name),
            showHat = player.getClientOption(ClientOption.SKIN_PARTS).hasHatsEnabled(),
            listOrder = player.playerListOrder
        )
        val previous = publishedEntries.put(player.uniqueId, entry)
        if (!force && previous == entry) return

        publisher.publishTablistEntry(entry)
    }

    private fun broadcast(packet: ClientboundSetPlayerTeamPacket) {
        Bukkit.getOnlinePlayers().forEach { player ->
            (player as CraftPlayer).handle.connection.send(packet)
        }
    }

}
