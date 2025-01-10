package app.simplecloud.plugin.prefixes.paper.packet

import app.simplecloud.plugin.prefixes.paper.packet.v1_17_1.ComponentPacketTeam
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.utility.MinecraftVersion
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player

abstract class PacketTeam(
    open var id: String,
    open var color: TextColor?,
    open var prefix: Component?,
    open var suffix: Component?,
    open var members: MutableList<Player> = mutableListOf(),
    open var priority: Int?,
) {
    abstract fun getUpdateIdPackets(id: String): List<PacketContainer>
    abstract fun getTeamUpdatePacket(mode: UpdateTeamMode): PacketContainer
    abstract fun getTeamDeletePacket(): PacketContainer
    abstract fun getUpdateTeamMembersPackets(): List<PacketContainer>
    abstract fun getModifyTeamMembersPacket(mode: UpdateTeamPlayersMode, players: List<Player>): PacketContainer
    abstract fun getUpdateDisplayNamePackets(): List<PacketContainer>
    abstract fun getUpdateDisplayNamePacket(player: Player): PacketContainer

    fun asComponent(player: Player): Component {
        return (prefix ?: Component.text("")).append(Component.text(player.name).color(color ?: NamedTextColor.WHITE))
            .append(suffix ?: Component.text(""))
    }

    companion object {
        fun create(
            id: String,
            color: TextColor?,
            prefix: Component?,
            suffix: Component?,
            priority: Int,
            members: MutableList<Player>
        ): PacketTeam {
            if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
                return ComponentPacketTeam(id, color, prefix, suffix, priority, members)
            }
            //TODO: add support for older minecraft versions
            throw NullPointerException("No packet wrapper for your server version was found. Please visit https://github.com/thesimplecloud/prefixes-plugin for more information.")
        }
    }
}

enum class UpdateTeamMode(private val mode: Int) {
    CREATE(0),
    UPDATE(2);

    fun getMode(): Int {
        return mode
    }
}

enum class UpdateTeamPlayersMode(private val mode: Int) {
    ADD(3),
    REMOVE(4);

    fun getMode(): Int {
        return mode
    }
}