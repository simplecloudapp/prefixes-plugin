package app.simplecloud.plugin.prefixes.paper.packet

import app.simplecloud.plugin.prefixes.api.PrefixesApi
import app.simplecloud.plugin.prefixes.paper.PaperPrefixesLoader
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import org.bukkit.plugin.Plugin

class PlayerCreatePacketAdapter(private val prefixesPlugin: Plugin, private val api: PrefixesApi) : PacketAdapter(
    prefixesPlugin,
    ListenerPriority.NORMAL,
    PacketType.Play.Client.CHAT_SESSION_UPDATE
) {

    override fun onPacketReceiving(event: PacketEvent) {
        if (event.packetType == PacketType.Play.Client.CHAT_SESSION_UPDATE && !api.hasViewer(event.player.uniqueId)) {
            api.registerViewer(event.player.uniqueId)
            PaperPrefixesLoader.applyFirstName(api, prefixesPlugin, event.player)
        }
    }
}