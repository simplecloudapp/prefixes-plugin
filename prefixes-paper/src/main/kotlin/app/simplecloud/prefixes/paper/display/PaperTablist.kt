package app.simplecloud.prefixes.paper.display

import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import com.mojang.authlib.GameProfile
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

    private val entries = ConcurrentHashMap<UUID, TablistEntry>()

    fun update(entry: TablistEntry) {
        if (Bukkit.getPlayer(entry.uniqueId) != null) return

        entries[entry.uniqueId] = entry
        broadcast(entry.toInfoPacket())
        broadcast(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(entry.toTeam(), true))
    }

    fun remove(id: UUID) {
        val entry = entries.remove(id) ?: return
        broadcast(ClientboundSetPlayerTeamPacket.createRemovePacket(entry.toTeam()))
        broadcast(ClientboundPlayerInfoRemovePacket(listOf(id)))
    }

    fun sync(player: Player) {
        val connection = (player as CraftPlayer).handle.connection

        entries.values.forEach { entry ->
            if (Bukkit.getPlayer(entry.uniqueId) != null) return@forEach
            connection.send(entry.toInfoPacket())
            connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(entry.toTeam(), true))
        }
    }

    private fun TablistEntry.toTeam() = PaperPlayerTeam(name, priority)

    private fun TablistEntry.toInfoPacket(): ClientboundPlayerInfoUpdatePacket {
        val entry = ClientboundPlayerInfoUpdatePacket.Entry(
            uniqueId,
            GameProfile(uniqueId, name),
            true,
            0,
            GameType.SURVIVAL,
            PaperAdventure.asVanilla(displayName),
            true,
            0,
            null
        )

        return ClientboundPlayerInfoUpdatePacket(
            EnumSet.of(
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME
            ),
            listOf(entry)
        )
    }

    private fun broadcast(packet: Packet<*>) {
        Bukkit.getOnlinePlayers().forEach { player ->
            (player as CraftPlayer).handle.connection.send(packet)
        }
    }
}
