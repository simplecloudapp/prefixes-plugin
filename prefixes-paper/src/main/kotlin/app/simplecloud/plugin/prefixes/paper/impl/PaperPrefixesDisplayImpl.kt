package app.simplecloud.plugin.prefixes.paper.impl

import app.simplecloud.plugin.prefixes.api.PrefixesDisplay
import app.simplecloud.plugin.prefixes.api.PrefixesPlayerData
import app.simplecloud.plugin.prefixes.paper.PaperPlayerTeam
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

class PaperPrefixesDisplayImpl(
    private val audience: Audience
) : PrefixesDisplay<Player> {

    private val displayed = mutableMapOf<UUID, PrefixesPlayerData>()

    override fun transition(
        player: Player,
        content: PrefixesPlayerData
    ) {
        displayed[player.uniqueId] = content
    }

    override fun apply(
        player: Player, content: PrefixesPlayerData
    ) {
        val handles = toHandles()
        if (!displayed.containsKey(player.uniqueId)) {
            create(player, content, handles)
        } else if (displayed[player.uniqueId]?.priority != content.priority) {
            recreate(player, content, handles)
        } else updateVisuals(player, content, handles)
        sendUpdateDisplayNamePackets(player, content.toFormattedName(player.name()), handles)
    }

    override fun getCurrent(player: Player): PrefixesPlayerData? {
        return displayed[player.uniqueId]
    }

    private fun updateVisuals(player: Player, content: PrefixesPlayerData, handles: List<ServerPlayer>) {
        displayed[player.uniqueId] = content
        sendUpdatePackets(PaperPlayerTeam(player, content), handles)
    }

    private fun recreate(player: Player, content: PrefixesPlayerData, handles: List<ServerPlayer>) {
        val oldTeam = PaperPlayerTeam(player, displayed[player.uniqueId] ?: return)
        val newTeam = PaperPlayerTeam(player, content)
        sendRemovePackets(oldTeam, handles)
        sendCreatePackets(player, newTeam, handles)
        displayed[player.uniqueId] = content
    }

    private fun create(player: Player, content: PrefixesPlayerData, handles: List<ServerPlayer>) {
        displayed[player.uniqueId] = content
        val team = PaperPlayerTeam(player, content)
        sendCreatePackets(player, team, handles)
    }

    override fun remove(player: Player) {
        val team = PaperPlayerTeam(player, displayed[player.uniqueId] ?: return)
        displayed.remove(player.uniqueId)
        val handles = toHandles()
        sendRemovePackets(team, handles)
        sendUpdateDisplayNamePackets(player, player.name(), handles)
    }

    override fun getAll(): Map<Player, PrefixesPlayerData> {
        return displayed.filter { Bukkit.getPlayer(it.key)?.isOnline ?: false }
            .map { Bukkit.getPlayer(it.key)!! to it.value }.toMap()
    }

    private fun sendCreatePackets(player: Player, team: PaperPlayerTeam, handles: List<ServerPlayer>) {
        val createTeam = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true)
        val addPlayers = ClientboundSetPlayerTeamPacket.createMultiplePlayerPacket(
            team, listOf(player.name), ClientboundSetPlayerTeamPacket.Action.ADD
        )
        handles.forEach {
            it.connection.send(createTeam)
            it.connection.send(addPlayers)
        }
    }

    private fun sendUpdatePackets(team: PaperPlayerTeam, handles: List<ServerPlayer>) {
        val updateTeam = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false)
        handles.forEach { it.connection.send(updateTeam) }
    }

    private fun sendRemovePackets(team: PaperPlayerTeam, handles: List<ServerPlayer>) {
        val deleteTeam = ClientboundSetPlayerTeamPacket.createRemovePacket(team)
        handles.forEach { it.connection.send(deleteTeam) }
    }

    private fun sendUpdateDisplayNamePackets(player: Player, name: Component, handles: List<ServerPlayer>) {
        val update = ClientboundPlayerInfoUpdatePacket(
            EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
            ClientboundPlayerInfoUpdatePacket.Entry(
                player.uniqueId, null, true, -1, GameType.ADVENTURE, PaperAdventure.asVanilla(name), true, -1, null
            )
        )
        handles.forEach { it.connection.send(update) }
    }

    private fun toHandles(
        currentAudience: Audience = audience, current: MutableList<ServerPlayer> = mutableListOf()
    ): MutableList<ServerPlayer> {
        val uniqueId = currentAudience.get(Identity.UUID)
        if (uniqueId.isPresent) {
            val player = Bukkit.getPlayer(uniqueId.get())
            if (player?.isOnline ?: false) {
                current.add((player as CraftPlayer).handle)
                return current
            }
        }
        currentAudience.forEachAudience { toHandles(it, current) }
        return current
    }
}