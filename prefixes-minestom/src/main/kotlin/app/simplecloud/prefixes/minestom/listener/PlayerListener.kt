package app.simplecloud.prefixes.minestom.listener

import app.simplecloud.prefixes.minestom.display.MinestomDisplayManager
import app.simplecloud.prefixes.minestom.display.MinestomTablist
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.utilities.PlayerDisplayFormatter
import net.kyori.adventure.text.Component
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerInputEvent
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
        node.addListener(PlayerInputEvent::class.java, ::onInput)
    }

    private fun onSpawn(event: PlayerSpawnEvent) {
        // The entity carrying the name tag stays behind in the previous instance.
        if (!event.isFirstSpawn) {
            manager.refreshNameTag(event.player)
            return
        }

        tablist.remove(event.player.uuid)
        manager.addPlayer(event.player)

        val config = prefixes.config.get()
        if (config.features.tablist && config.sync.enabled && config.sync.channels.tablist) {
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
        val data = manager.getPlayer(player.uuid) ?: return
        val displayName = PlayerDisplayFormatter.displayName(data, player.username, features.displayName)

        val message = PlayerDisplayFormatter.formatChatMessage(
            data,
            player.username,
            Component.text(event.rawMessage),
            displayName
        )

        event.formattedMessage = message
        prefixes.sync?.publisher?.publishChatMessage(message)
    }

    private fun onInput(event: PlayerInputEvent) {
        when {
            event.hasPressedShiftKey() -> manager.setSneaking(event.player, true)
            event.hasReleasedShiftKey() -> manager.setSneaking(event.player, false)
        }
    }
}
