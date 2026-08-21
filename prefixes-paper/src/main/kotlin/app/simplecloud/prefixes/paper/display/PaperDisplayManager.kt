package app.simplecloud.prefixes.paper.display

import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.config.FeaturesConfig
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
    private val teams = ConcurrentHashMap<UUID, PaperPlayerTeam>()
    private val entries = ConcurrentHashMap<UUID, TablistEntry>()

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
            Bukkit.getScheduler().runTask(plugin, Runnable { applyPrefixData(player, data) })
        }.exceptionally { _ ->
            plugin.logger.warning("Failed to update prefix data of ${player.name}")
            null
        }
    }

    fun removePlayer(player: Player) {
        cache.remove(player.uniqueId)
        removeTeam(player.uniqueId)

        entries.remove(player.uniqueId)
        prefixes.sync?.publisher?.publishTablistRemove(player.uniqueId)
    }

    private fun applyPrefixData(player: Player, data: PrefixesPlayerData) {
        if (!player.isOnline) return
        cache[player.uniqueId] = data

        val features = prefixes.config.get().features
        val displayName = PlayerDisplayFormatter.displayName(
            data,
            player.name,
            features.displayName
        )

        player.playerListName(
            when {
                features.tablist -> PlayerDisplayFormatter.formatTablistName(data, displayName)
                else -> null
            }
        )

        updateTeam(player, data, features)
        updateCustomName(player, displayName, features)

        publish(player, data, displayName)
    }

    private fun updateTeam(player: Player, data: PrefixesPlayerData, features: FeaturesConfig) {
        removeTeam(player.uniqueId)

        val team = createTeam(player, data, features) ?: return
        teams[player.uniqueId] = team
        broadcast(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true))
    }

    private fun createTeam(player: Player, data: PrefixesPlayerData, features: FeaturesConfig): PaperPlayerTeam? {
        return when {
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
    }

    private fun removeTeam(id: UUID) {
        val team = teams.remove(id) ?: return
        broadcast(ClientboundSetPlayerTeamPacket.createRemovePacket(team))
    }

    private fun updateCustomName(player: Player, displayName: Component, features: FeaturesConfig) {
        val name = customNameManager.forEntity(player)
        when {
            features.displayName -> {
                name.setName(displayName)
                name.setHidden(false)
            }

            else -> name.setHidden(true)
        }
    }

    fun syncPlayers(player: Player) {
        val connection = (player as CraftPlayer).handle.connection

        teams.values.forEach { team ->
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

    private fun publish(player: Player, data: PrefixesPlayerData, displayName: Component, force: Boolean = false) {
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
        val previous = entries.put(player.uniqueId, entry)
        if (!force && previous == entry) return

        publisher.publishTablistEntry(entry)
    }

    private fun broadcast(packet: ClientboundSetPlayerTeamPacket) {
        Bukkit.getOnlinePlayers().forEach { player ->
            (player as CraftPlayer).handle.connection.send(packet)
        }
    }

}
