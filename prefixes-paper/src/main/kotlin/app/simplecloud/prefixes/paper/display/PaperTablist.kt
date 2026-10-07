package app.simplecloud.prefixes.paper.display

import app.simplecloud.prefixes.shared.sync.tablist.SourcedTablistEntry
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import io.papermc.paper.adventure.PaperAdventure
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.world.level.GameType
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.EnumSet
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PaperTablist {

    private val entries = ConcurrentHashMap<UUID, SourcedTablistEntry>()

    fun update(publisherId: String, entry: TablistEntry) {
        if (Bukkit.getPlayer(entry.uniqueId) != null) return

        val previous = entries.put(entry.uniqueId, SourcedTablistEntry(publisherId, entry))?.entry
        if (previous != entry) {
            broadcast(createInfoPacket(listOf(entry)))
        }
        if (previous == null || previous.name != entry.name || previous.priority != entry.priority) {
            if (previous != null) broadcast(ClientboundSetPlayerTeamPacket.createRemovePacket(createTeam(previous)))
            broadcast(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(createTeam(entry), true))
        }
    }

    fun remove(id: UUID) {
        val sourced = entries.remove(id) ?: return
        hide(sourced.entry)
    }

    fun remove(publisherId: String, id: UUID) {
        val sourced = entries[id] ?: return
        if (sourced.publisherId != publisherId) return
        if (entries.remove(id, sourced)) hide(sourced.entry)
    }

    fun clear() {
        entries.keys.toList().forEach(::remove)
    }

    fun sync(player: Player) {
        val visible = entries.values.map(SourcedTablistEntry::entry).filter { entry -> Bukkit.getPlayer(entry.uniqueId) == null }

        visible.chunked(100).forEach { batch -> send(player, createInfoPacket(batch)) }
        visible.forEach { entry -> send(player, ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(createTeam(entry), true)) }
    }

    private fun hide(entry: TablistEntry) {
        broadcast(ClientboundSetPlayerTeamPacket.createRemovePacket(createTeam(entry)))
        if (Bukkit.getPlayer(entry.uniqueId) == null) {
            broadcast(ClientboundPlayerInfoRemovePacket(listOf(entry.uniqueId)))
        }
    }

    private fun createTeam(entry: TablistEntry): PaperPlayerTeam = PaperPlayerTeam(entry.name, entry.priority, hideNameTag = true)

    private fun createInfoPacket(tablistEntries: Collection<TablistEntry>): ClientboundPlayerInfoUpdatePacket {
        val packetEntries = tablistEntries.map { entry ->
            ClientboundPlayerInfoUpdatePacket.Entry(
                entry.uniqueId,
                createGameProfile(entry),
                true,
                entry.latency,
                GameType.valueOf(entry.gameMode.name),
                PaperAdventure.asVanilla(entry.displayName),
                entry.showHat,
                entry.listOrder,
                null
            )
        }

        return ClientboundPlayerInfoUpdatePacket(getUpdateActions(), packetEntries)
    }

    private fun getUpdateActions(): EnumSet<Action> = EnumSet.of(
        Action.ADD_PLAYER,
        Action.UPDATE_LISTED,
        Action.UPDATE_GAME_MODE,
        Action.UPDATE_LATENCY,
        Action.UPDATE_DISPLAY_NAME,
        Action.UPDATE_LIST_ORDER,
        Action.UPDATE_HAT
    )

    private fun createGameProfile(entry: TablistEntry): GameProfile {
        val properties = ImmutableMultimap.builder<String, Property>()
        entry.profileProperties.forEach { property ->
            properties.put(property.name, Property(property.name, property.value, property.signature))
        }
        return GameProfile(entry.uniqueId, entry.name, PropertyMap(properties.build()))
    }

    private fun broadcast(packet: Packet<*>) {
        Bukkit.getOnlinePlayers().forEach { player -> send(player, packet) }
    }

    private fun send(player: Player, packet: Packet<*>) {
        (player as CraftPlayer).handle.connection.send(packet)
    }
}
