package app.simplecloud.prefixes.minestom.listener

import app.simplecloud.prefixes.minestom.display.MinestomDisplayManager
import app.simplecloud.prefixes.minestom.display.MinestomTablist
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.utilities.PlayerDisplayFormatter
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent

class PlayerListener(
    private val prefixes: Prefixes,
    private val manager: MinestomDisplayManager,
    private val tablist: MinestomTablist
) {

    fun register(node: EventNode<Event>) {
        node.addListener(PlayerSpawnEvent::class.java, ::onSpawn)
        node.addListener(PlayerDisconnectEvent::class.java, ::onDisconnect)
        node.addListener(PlayerChatEvent::class.java, ::onChat)
    }

    private fun onSpawn(event: PlayerSpawnEvent) {
        if (!event.isFirstSpawn) {
            manager.refreshNameTag(event.player)
            return
        }

        tablist.remove(event.player.uuid)
        manager.updatePlayer(event.player)
        manager.addViewer(event.player)

        if (prefixes.config.get().isTablistSynced()) {
            tablist.sync(event.player)
        }
    }

    private fun onDisconnect(event: PlayerDisconnectEvent) {
        manager.removePlayer(event.player)
    }

    private fun onChat(event: PlayerChatEvent) {
        val features = prefixes.config.get().features
        if (!features.chat) return

        val player = event.player
        val data = prefixes.dataRegistry.getData(player.uuid) ?: return
        val rawMessage = Component.text(event.rawMessage)
        val message = PlayerDisplayFormatter.formatChatMessage(data, player.username, rawMessage, features.displayName)

        val viewerMessages = event.recipients.mapNotNull { viewer ->
            val viewerData = prefixes.dataRegistry.getViewerData(player.uuid, viewer.uuid) ?: return@mapNotNull null
            viewer to PlayerDisplayFormatter.formatChatMessage(viewerData, player.username, rawMessage, features.displayName)
        }

        event.recipients.removeAll(viewerMessages.map { (viewer, _) -> viewer }.toSet())
        event.formattedMessage = message

        MinecraftServer.getSchedulerManager().scheduleNextTick {
            if (event.isCancelled) return@scheduleNextTick

            viewerMessages.forEach { (viewer, viewerMessage) -> viewer.sendMessage(viewerMessage) }
            prefixes.sync?.publisher?.publishChatMessage(message)
        }
    }
}
