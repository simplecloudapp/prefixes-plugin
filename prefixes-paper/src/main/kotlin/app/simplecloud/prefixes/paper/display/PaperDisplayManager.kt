package app.simplecloud.prefixes.paper.display

import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.data.ViewerKey
import app.simplecloud.prefixes.shared.sync.tablist.ProfileProperty
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import app.simplecloud.prefixes.shared.sync.tablist.TablistGameMode
import app.simplecloud.prefixes.shared.PrefixesConstants
import app.simplecloud.prefixes.shared.utilities.PrefixesCoroutineDetails
import app.simplecloud.prefixes.shared.utilities.PlayerDisplayFormatter
import com.destroystokyo.paper.ClientOption
import io.papermc.paper.adventure.PaperAdventure
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import space.chunks.customname.api.CustomNameManager
import java.util.EnumSet
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PaperDisplayManager(
    private val plugin: Plugin,
    private val prefixes: Prefixes,
    private val customNameManager: CustomNameManager<Entity>
) {

    private val registry = prefixes.dataRegistry

    private val teams = ConcurrentHashMap<ViewerKey, PaperPlayerTeam>()
    private val entries = ConcurrentHashMap<UUID, TablistEntry>()

    fun addPlayer(player: Player) {
        updatePlayer(player)
        player.scheduler.run(plugin, {
            if (!registry.isLoaded(player.uniqueId)) publish(player)
        }, null)
    }

    fun updatePlayer(player: Player) {
        PrefixesConstants.SCOPE.launch(PrefixesCoroutineDetails("${player.name} (${player.uniqueId})", "update player")) {
            val group = prefixes.api.getPrimaryGroup(player.uniqueId).await()
            player.scheduler.run(plugin, {
                registry.loadGroup(player.uniqueId, group)
                show(player)
            }, null)
        }
    }

    fun removePlayer(player: Player) {
        val id = player.uniqueId
        registry.remove(id)
        teams.keys.filter { key -> key.target == id || key.viewer == id }.forEach { key ->
            if (key.viewer == id) teams.remove(key) else removeTeam(key)
        }

        entries.remove(id)
        prefixes.sync?.publisher?.publishTablistRemove(id)
    }

    fun clear() {
        entries.clear()
        teams.keys.toList().forEach(::removeTeam)
        Bukkit.getOnlinePlayers().forEach { player -> player.playerListName(null) }
    }

    fun show(player: Player) {
        if (!player.isOnline) return
        val data = registry.getData(player.uniqueId) ?: return
        val features = prefixes.config.get().features

        player.playerListName(if (features.tablist) PlayerDisplayFormatter.formatTablistName(data, player.name) else null)
        val team = createTeam(player, data)
        Bukkit.getOnlinePlayers().forEach { viewer -> showTo(player, viewer, team) }

        val name = customNameManager.forEntity(player)
        name.setName { viewer -> (registry.getViewerData(player.uniqueId, viewer) ?: data).displayName }
        name.setHidden(!features.displayName)

        publish(player)
    }

    fun addViewer(viewer: Player) {
        Bukkit.getOnlinePlayers().forEach { player ->
            val data = registry.getData(player.uniqueId) ?: return@forEach
            showTo(player, viewer, createTeam(player, data))
        }
    }

    fun publishAll(force: Boolean = false) {
        Bukkit.getOnlinePlayers().forEach { player ->
            player.scheduler.run(plugin, {
                if (registry.isLoaded(player.uniqueId)) publish(player, force)
            }, null)
        }
    }

    private fun showTo(player: Player, viewer: Player, team: PaperPlayerTeam?) {
        val viewerData = registry.getViewerData(player.uniqueId, viewer.uniqueId)
        if (viewerData == null) {
            updateTeam(player, viewer, team)
            return
        }

        updateTeam(player, viewer, createTeam(player, viewerData))
        if (prefixes.config.get().features.tablist) {
            send(viewer, createListNamePacket(player, PlayerDisplayFormatter.formatTablistName(viewerData, player.name)))
        }
    }

    private fun publish(player: Player, force: Boolean = false) {
        if (!prefixes.config.get().isTablistSynced()) return
        val publisher = prefixes.sync?.publisher ?: return

        val data = registry.getData(player.uniqueId)
        val entry = TablistEntry(
            uniqueId = player.uniqueId,
            name = player.name,
            displayName = if (data == null) Component.text(player.name) else PlayerDisplayFormatter.formatTablistName(data, player.name),
            priority = data?.priority ?: 0,
            profileProperties = player.playerProfile.properties.map { property -> ProfileProperty(property.name, property.value, property.signature) },
            latency = player.ping,
            gameMode = TablistGameMode.valueOf(player.gameMode.name),
            showHat = player.getClientOption(ClientOption.SKIN_PARTS).hasHatsEnabled(),
            listOrder = player.playerListOrder
        )

        val previous = entries.put(player.uniqueId, entry)
        if (force || previous != entry) {
            publisher.publishTablistEntry(entry)
        }
    }

    private fun createTeam(player: Player, data: PrefixesPlayerData): PaperPlayerTeam? {
        val features = prefixes.config.get().features
        return when {
            features.tablist -> PaperPlayerTeam(player.name, data.priority, data.prefix, data.suffix, data.color, features.displayName)
            features.displayName -> PaperPlayerTeam(player.name, priority = 0, hideNameTag = true)
            else -> null
        }
    }

    private fun updateTeam(player: Player, viewer: Player, team: PaperPlayerTeam?) {
        teams.compute(ViewerKey(player.uniqueId, viewer.uniqueId)) { _, current ->
            if (current != null) send(viewer, ClientboundSetPlayerTeamPacket.createRemovePacket(current))
            if (team != null) send(viewer, ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true))
            team
        }
    }

    private fun removeTeam(key: ViewerKey) {
        teams.computeIfPresent(key) { _, team ->
            val viewer = Bukkit.getPlayer(key.viewer)
            if (viewer != null) send(viewer, ClientboundSetPlayerTeamPacket.createRemovePacket(team))
            null
        }
    }

    private fun createListNamePacket(player: Player, name: Component): ClientboundPlayerInfoUpdatePacket {
        val handle = (player as CraftPlayer).handle
        val entry = ClientboundPlayerInfoUpdatePacket.Entry(
            player.uniqueId,
            handle.gameProfile,
            true,
            player.ping,
            handle.gameMode.gameModeForPlayer,
            PaperAdventure.asVanilla(name),
            true,
            player.playerListOrder,
            null
        )
        return ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME), listOf(entry))
    }

    private fun send(viewer: Player, packet: Packet<*>) {
        (viewer as CraftPlayer).handle.connection.send(packet)
    }
}
