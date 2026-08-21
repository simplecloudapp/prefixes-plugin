package app.simplecloud.prefixes.paper.display

import app.simplecloud.prefixes.shared.sync.tablist.ProfileProperty
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import app.simplecloud.prefixes.shared.sync.tablist.TablistGameMode
import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import io.papermc.paper.adventure.PaperAdventure
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
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
        val actions = getActions(entry, previous)

        if (actions.isNotEmpty()) {
            broadcast(createInfoPacket(listOf(entry), actions))
        }
        if (previous == null || previous.name != entry.name || previous.priority != entry.priority) {
            if (previous != null) {
                broadcast(ClientboundSetPlayerTeamPacket.createRemovePacket(createTeam(previous)))
            }
            broadcast(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(createTeam(entry), true))
        }
    }

    fun remove(id: UUID) {
        removeEntry(null, id)
    }

    fun remove(publisherId: String, id: UUID) {
        removeEntry(publisherId, id)
    }

    fun clear() {
        entries.keys.toList().forEach(::remove)
    }

    private fun removeEntry(publisherId: String?, id: UUID) {
        val sourcedEntry = entries[id] ?: return
        if (publisherId != null && sourcedEntry.publisherId != publisherId) return
        if (!entries.remove(id, sourcedEntry)) return

        if (Bukkit.getPlayer(id) != null) return

        val entry = sourcedEntry.entry
        broadcast(ClientboundSetPlayerTeamPacket.createRemovePacket(createTeam(entry)))
        broadcast(ClientboundPlayerInfoRemovePacket(listOf(id)))
    }

    fun sync(player: Player) {
        val connection = (player as CraftPlayer).handle.connection

        val visibleEntries = entries.values
            .map(SourcedTablistEntry::entry)
            .filter { Bukkit.getPlayer(it.uniqueId) == null }

        visibleEntries.chunked(100).forEach { batch ->
            connection.send(createInfoPacket(batch, getFullUpdateActions()))
        }
        visibleEntries.forEach { entry ->
            connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(createTeam(entry), true))
        }
    }

    private fun createTeam(entry: TablistEntry) = PaperPlayerTeam(entry.name, entry.priority)

    private fun createInfoPacket(
        tablistEntries: Collection<TablistEntry>,
        actions: EnumSet<ClientboundPlayerInfoUpdatePacket.Action>
    ): ClientboundPlayerInfoUpdatePacket {
        val packet = tablistEntries.map { entry ->
            ClientboundPlayerInfoUpdatePacket.Entry(
                entry.uniqueId,
                createGameProfile(entry),
                true,
                entry.latency,
                getGameType(entry.gameMode),
                PaperAdventure.asVanilla(entry.displayName),
                entry.showHat,
                entry.listOrder,
                null
            )
        }

        return ClientboundPlayerInfoUpdatePacket(actions, packet)
    }

    private fun getActions(
        entry: TablistEntry,
        previous: TablistEntry?
    ): EnumSet<ClientboundPlayerInfoUpdatePacket.Action> {
        if (previous == null ||
            previous.name != entry.name ||
            previous.profileProperties != entry.profileProperties
        ) {
            return getFullUpdateActions()
        }

        val actions = EnumSet.noneOf(ClientboundPlayerInfoUpdatePacket.Action::class.java)
        if (previous.gameMode != entry.gameMode) {
            actions.add(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE)
        }
        if (previous.latency != entry.latency) {
            actions.add(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY)
        }
        if (previous.displayName != entry.displayName) {
            actions.add(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME)
        }
        if (previous.listOrder != entry.listOrder) {
            actions.add(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER)
        }
        if (previous.showHat != entry.showHat) {
            actions.add(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_HAT)
        }
        return actions
    }

    private fun getFullUpdateActions(): EnumSet<ClientboundPlayerInfoUpdatePacket.Action> =
        EnumSet.of(
            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_HAT
        )

    private fun createGameProfile(entry: TablistEntry): GameProfile {
        val properties = ImmutableMultimap.builder<String, Property>()

        entry.profileProperties.forEach { property ->
            properties.put(property.name, createProperty(property))
        }

        return GameProfile(entry.uniqueId, entry.name, PropertyMap(properties.build()))
    }

    private fun createProperty(property: ProfileProperty): Property {
        val signature = property.signature ?: return Property(property.name, property.value)
        return Property(property.name, property.value, signature)
    }

    private fun getGameType(gameMode: TablistGameMode): GameType = when (gameMode) {
        TablistGameMode.SURVIVAL -> GameType.SURVIVAL
        TablistGameMode.CREATIVE -> GameType.CREATIVE
        TablistGameMode.ADVENTURE -> GameType.ADVENTURE
        TablistGameMode.SPECTATOR -> GameType.SPECTATOR
    }

    private fun broadcast(packet: Packet<*>) {
        Bukkit.getOnlinePlayers().forEach { player ->
            (player as CraftPlayer).handle.connection.send(packet)
        }
    }
}
