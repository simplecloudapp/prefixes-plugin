package app.simplecloud.prefixes.minestom.display

import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.data.ViewerKey
import app.simplecloud.prefixes.shared.sync.tablist.ProfileProperty
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import app.simplecloud.prefixes.shared.sync.tablist.TablistGameMode
import app.simplecloud.prefixes.shared.PrefixesConstants
import app.simplecloud.prefixes.shared.utilities.PrefixesCoroutineDetails
import app.simplecloud.prefixes.shared.utilities.PlayerDisplayFormatter
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket
import net.minestom.server.network.packet.server.play.TeamsPacket
import space.chunks.customname.api.CustomNameManager
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MinestomDisplayManager(
    private val prefixes: Prefixes,
    private val customNameManager: CustomNameManager<Entity>
) {

    private val registry = prefixes.dataRegistry

    private val teams = ConcurrentHashMap<ViewerKey, TeamsPacket>()
    private val entries = ConcurrentHashMap<UUID, TablistEntry>()

    fun updatePlayer(player: Player) {
        PrefixesConstants.SCOPE.launch(PrefixesCoroutineDetails("${player.username} (${player.uuid})", "update player")) {
            val group = prefixes.api.getPrimaryGroup(player.uuid).await()
            MinecraftServer.getSchedulerManager().scheduleNextTick {
                if (!player.isOnline) return@scheduleNextTick
                registry.loadGroup(player.uuid, group)
                render(player)
            }
        }
    }

    fun removePlayer(player: Player) {
        val id = player.uuid
        registry.remove(id)
        teams.keys.filter { key -> key.target == id || key.viewer == id }.forEach { key ->
            if (key.viewer == id) teams.remove(key) else removeTeam(key)
        }
        customNameManager.unregister(player)

        entries.remove(id)
        prefixes.sync?.publisher?.publishTablistRemove(id)
    }

    fun clear() {
        entries.clear()
        teams.keys.toList().forEach(::removeTeam)
        onlinePlayers().forEach { player -> player.displayName = null }
    }

    fun render(player: Player) {
        if (!player.isOnline) return
        val data = registry.getData(player.uuid) ?: return

        player.displayName = if (prefixes.config.get().features.tablist) PlayerDisplayFormatter.formatTablistName(data, player.username) else null

        val team = createTeam(player, data)
        onlinePlayers().forEach { viewer -> renderFor(player, viewer, team) }

        updateNameTag(player, data)
        publish(player)
    }

    fun refreshNameTag(player: Player) {
        val data = registry.getData(player.uuid) ?: return
        updateNameTag(player, data)
    }

    fun addViewer(viewer: Player) {
        onlinePlayers().forEach { player ->
            val data = registry.getData(player.uuid) ?: return@forEach
            renderFor(player, viewer, createTeam(player, data))
        }
    }

    fun publishAll(force: Boolean = false) {
        onlinePlayers().forEach { player -> publish(player, force) }
    }

    private fun renderFor(player: Player, viewer: Player, team: TeamsPacket?) {
        val viewerData = registry.getViewerData(player.uuid, viewer.uuid)
        if (viewerData == null) {
            updateTeam(player, viewer, team)
            return
        }

        updateTeam(player, viewer, createTeam(player, viewerData))
        if (prefixes.config.get().features.tablist) {
            viewer.sendPacket(createListNamePacket(player, PlayerDisplayFormatter.formatTablistName(viewerData, player.username)))
        }
    }

    private fun updateNameTag(player: Player, data: PrefixesPlayerData) {
        val name = customNameManager.forEntity(player)
        name.setName { viewer -> (registry.getViewerData(player.uuid, viewer) ?: data).displayName }
        name.setHidden(!prefixes.config.get().features.displayName)
    }

    private fun publish(player: Player, force: Boolean = false) {
        if (!prefixes.config.get().isTablistSynced()) return
        val publisher = prefixes.sync?.publisher ?: return
        val data = registry.getData(player.uuid) ?: return

        val skin = player.skin
        val entry = TablistEntry(
            uniqueId = player.uuid,
            name = player.username,
            displayName = PlayerDisplayFormatter.formatTablistName(data, player.username),
            priority = data.priority,
            profileProperties = if (skin == null) emptyList() else listOf(ProfileProperty("textures", skin.textures(), skin.signature())),
            latency = player.latency,
            gameMode = TablistGameMode.valueOf(player.gameMode.name),
            showHat = (player.settings.displayedSkinParts.toInt() and 0x40) != 0,
            listOrder = player.listOrder
        )

        val previous = entries.put(player.uuid, entry)
        if (force || previous != entry) {
            publisher.publishTablistEntry(entry)
        }
    }

    private fun createTeam(player: Player, data: PrefixesPlayerData): TeamsPacket? {
        val features = prefixes.config.get().features
        return when {
            features.tablist -> MinestomPlayerTeam.createPacket(player.username, data.priority, data.prefix, data.suffix, data.color, features.displayName)
            features.displayName -> MinestomPlayerTeam.createPacket(player.username, priority = 0, hideNameTag = true)
            else -> null
        }
    }

    private fun updateTeam(player: Player, viewer: Player, team: TeamsPacket?) {
        val key = ViewerKey(player.uuid, viewer.uuid)
        removeTeam(key)
        if (team == null) return

        teams[key] = team
        viewer.sendPacket(team)
    }

    private fun removeTeam(key: ViewerKey) {
        val team = teams.remove(key) ?: return
        val viewer = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(key.viewer) ?: return
        viewer.sendPacket(MinestomPlayerTeam.removePacket(team))
    }

    private fun createListNamePacket(player: Player, name: Component): PlayerInfoUpdatePacket {
        val entry = PlayerInfoUpdatePacket.Entry(
            player.uuid,
            player.username,
            emptyList(),
            true,
            player.latency,
            player.gameMode,
            name,
            null,
            player.listOrder,
            true
        )
        return PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, entry)
    }

    private fun onlinePlayers(): Collection<Player> = MinecraftServer.getConnectionManager().onlinePlayers
}
