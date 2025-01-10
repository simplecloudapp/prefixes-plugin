package app.simplecloud.plugin.prefixes.paper.packet.v1_17_1

import app.simplecloud.plugin.prefixes.shared.ComponentSerializerImpl
import app.simplecloud.plugin.prefixes.paper.packet.PacketTeam
import app.simplecloud.plugin.prefixes.paper.packet.UpdateTeamMode
import app.simplecloud.plugin.prefixes.paper.packet.UpdateTeamPlayersMode
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.utility.MinecraftReflection
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedGameProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*

class ComponentPacketTeam(
    id: String,
    color: TextColor?,
    prefix: Component?,
    suffix: Component?,
    priority: Int,
    members: MutableList<Player> = mutableListOf()
) : PacketTeam(id, color, prefix, suffix,  members, priority) {
    override fun getUpdateIdPackets(id: String): List<PacketContainer> {
        val toReturn = mutableListOf<PacketContainer>()
        toReturn.add(getTeamDeletePacket())
        this.id = id
        toReturn.add(getTeamUpdatePacket(UpdateTeamMode.CREATE))
        return toReturn
    }

    override fun getTeamUpdatePacket(mode: UpdateTeamMode): PacketContainer {
        val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM)
        packet.integers.write(0, mode.getMode())
        if (mode == UpdateTeamMode.CREATE)
            packet.getSpecificModifier(Collection::class.java).write(0, members.map { it.name })

        packet.strings.write(0, id)
        if (packet.optionalStructures.size() > 0) {
            val optional = packet.optionalStructures.read(0).get()
            if (prefix != null) optional.chatComponents.write(1, ComponentSerializerImpl.serializeToPacket(prefix!!))
            if (suffix != null) optional.chatComponents.write(2, ComponentSerializerImpl.serializeToPacket(suffix!!))
            optional.getEnumModifier(ChatColor::class.java, MinecraftReflection.getMinecraftClass("EnumChatFormat"))
                .write(
                    0,
                    if (color != null) ChatColor.valueOf(
                        NamedTextColor.nearestTo(color ?: NamedTextColor.WHITE).toString().uppercase()
                    ) else ChatColor.GRAY
                )
            packet.optionalStructures.write(0, Optional.of(optional))
        }
        return packet
    }

    override fun getTeamDeletePacket(): PacketContainer {
        val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM)
        if (packet.integers.size() > 1) {
            packet.integers.write(1, 1)
        } else if (packet.integers.size() > 0) {
            packet.integers.write(0, 1)
        }
        packet.strings.write(0, id)
        return packet
    }

    override fun getUpdateTeamMembersPackets(): List<PacketContainer> {
        return listOf(
            getModifyTeamMembersPacket(UpdateTeamPlayersMode.REMOVE, members),
            getModifyTeamMembersPacket(UpdateTeamPlayersMode.ADD, members)
        )
    }

    override fun getModifyTeamMembersPacket(mode: UpdateTeamPlayersMode, players: List<Player>): PacketContainer {
        val packet = PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM)
        if (packet.integers.size() > 1) {
            packet.integers.write(1, mode.getMode())
        } else if (packet.integers.size() > 0) {
            packet.integers.write(0, mode.getMode())
        }
        packet.strings.write(0, id)
        packet.getSpecificModifier(Collection::class.java).write(0, players.map { it.name })
        return packet
    }

    override fun getUpdateDisplayNamePackets(): List<PacketContainer> {
        return members.toList().map { getUpdateDisplayNamePacket(it) }
    }

    override fun getUpdateDisplayNamePacket(player: Player): PacketContainer {
        val packet = PacketContainer(PacketType.Play.Server.PLAYER_INFO)
        packet.playerInfoActions.write(0, EnumSet.of(EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME))
        val infoData = mutableListOf<PlayerInfoData>()
        val profile = WrappedGameProfile.fromPlayer(player)
        infoData.add(
            PlayerInfoData(
                profile,
                player.ping,
                EnumWrappers.NativeGameMode.fromBukkit(player.gameMode),
                ComponentSerializerImpl.serializeToPacket(constructDisplayName(player))
            )
        )
        packet.playerInfoDataLists.write(1, infoData)
        return packet
    }

    private fun constructDisplayName(player: Player): Component {
        if (!members.contains(player)) return Component.text(player.name)
        var base = if (prefix != null) prefix!!.append(
            Component.text(player.name).color(color)
        ) else Component.text(player.name)
        if (suffix != null) base = base.append(suffix!!)
        return base
    }
}