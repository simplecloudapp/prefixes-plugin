package app.simplecloud.prefixes.paper.display

import app.simplecloud.prefixes.shared.sync.tablist.SyncedTablist
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

class PaperTablist : SyncedTablist<Player, Packet<*>>() {

    override fun isOnline(id: UUID): Boolean = Bukkit.getPlayer(id) != null

    override fun createInfoPacket(entries: List<TablistEntry>): Packet<*> {
        val packetEntries = entries.map { entry ->
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

    override fun createInfoRemovePacket(id: UUID): Packet<*> = ClientboundPlayerInfoRemovePacket(listOf(id))

    override fun createTeamPacket(entry: TablistEntry): Packet<*> =
        ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(createTeam(entry), true)

    override fun createTeamRemovePacket(entry: TablistEntry): Packet<*> =
        ClientboundSetPlayerTeamPacket.createRemovePacket(createTeam(entry))

    override fun send(player: Player, packet: Packet<*>) {
        (player as CraftPlayer).handle.connection.send(packet)
    }

    override fun broadcast(packet: Packet<*>) {
        Bukkit.getOnlinePlayers().forEach { player -> send(player, packet) }
    }

    private fun createTeam(entry: TablistEntry): PaperPlayerTeam = PaperPlayerTeam(entry.name, entry.priority, hideNameTag = true)

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
}
