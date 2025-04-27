package app.simplecloud.plugin.prefixes.paper.impl

import app.simplecloud.plugin.prefixes.api.PrefixesApi
import app.simplecloud.plugin.prefixes.api.PrefixesConfig
import app.simplecloud.plugin.prefixes.api.PrefixesGroup
import app.simplecloud.plugin.prefixes.api.PrefixesPlayerData
import app.simplecloud.plugin.prefixes.paper.event.PrefixesConfigureEvent
import app.simplecloud.plugin.prefixes.paper.event.PrefixesConfiguredEvent
import app.simplecloud.plugin.prefixes.shared.MiniMessageImpl
import io.papermc.paper.event.player.AsyncChatEvent
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.jetbrains.annotations.ApiStatus
import space.chunks.customname.api.CustomNameManager

open class PaperPrefixesApiImpl(private val plugin: Plugin, private val name: CustomNameManager) : PrefixesApi<Player>,
    Listener {

    protected val storedGroups = mutableListOf<PrefixesGroup>()
    private val display = PaperPrefixesGlobalDisplayImpl()
    private lateinit var config: PrefixesConfig

    @ApiStatus.Internal
    override fun registerAudience(audience: Audience) {
        audience.toSingleAudiences().forEach {
            display.register(it, PaperPrefixesDisplayImpl(it))
        }
    }

    @ApiStatus.Internal
    override fun hasAudience(audience: Audience): Boolean {
        return !audience.toSingleAudiences().any { !display.getDisplay(it).isPresent }
    }

    @ApiStatus.Internal
    override fun removeAudience(audience: Audience) {
        audience.toSingleAudiences().forEach {
            display.remove(it)
        }
    }

    override fun setWholeName(
        player: Player, group: PrefixesGroup, audience: Audience?
    ) {
        setWholeName(player, groupToData(group), audience)
    }

    override fun setWholeName(
        player: Player, groupName: String, audience: Audience?
    ) {
        setWholeName(player, storedGroups.firstOrNull { it.getName() == groupName } ?: return, audience)
    }

    override fun setWholeName(
        player: Player, prefix: Component, color: TextColor, suffix: Component, priority: Int, audience: Audience?
    ) {
        setWholeName(player, PrefixesPlayerData(prefix, suffix, color, priority), audience)
    }

    override fun setWholeName(
        player: Player, data: PrefixesPlayerData, audience: Audience?
    ) {
        if (audience == null) {
            display.apply(player, data, audience)
            return
        }
        audience.toSingleAudiences().forEach {
            display.apply(player, data, it)
        }
    }

    override fun editWholeName(
        player: Player, audience: Audience?, action: (PrefixesPlayerData) -> Unit
    ) {
        val current = display.getCurrent(player, audience) ?: return
        action(current)
        setWholeName(player, current, audience)
    }

    override fun setPrefix(
        player: Player, prefix: Component, audience: Audience?
    ) {
        editWholeName(player, audience) {
            it.prefix = prefix
        }
    }

    override fun setSuffix(
        player: Player, suffix: Component, audience: Audience?
    ) {
        editWholeName(player, audience) {
            it.suffix = suffix
        }
    }

    override fun getGroups(): List<PrefixesGroup> {
        return storedGroups.toList()
    }

    override fun getHighestGroup(player: Player): PrefixesGroup {
        return storedGroups.filter { it.containsPlayer(player.uniqueId) }.minByOrNull { it.getPriority() }
            ?: storedGroups.first()
    }

    override fun addGroup(group: PrefixesGroup) {
        storedGroups.add(group)
    }

    override fun setColor(
        player: Player, color: TextColor, audience: Audience?
    ) {
        editWholeName(player, audience) {
            it.color = color
        }
    }

    override fun setConfig(config: PrefixesConfig) {
        this.config = config
    }

    override fun formatChatMessage(
        target: Player, format: String, message: Component, audience: Audience?
    ): Component {
        val usedDisplay = if (audience != null) display.getDisplay(audience)
            .orElseGet { display.getDefaultDisplay() } else display.getDefaultDisplay() ?: return message
        val data = usedDisplay.getCurrent(target) ?: display.getDefaultDisplay()?.getCurrent(target)
        val tags = mutableListOf<TagResolver>()
        if (data != null) {
            tags.add(Placeholder.component("prefix", data.prefix))
            tags.add(Placeholder.component("suffix", data.suffix))
            tags.add(
                Placeholder.component(
                    "name_colored", Component.text(target.name).color(data.color)
                )
            )
            tags.add(Placeholder.unparsed("name", target.name))
        } else {
            tags.add(Placeholder.unparsed("name", target.name))
        }
        tags.add(Placeholder.component("message", message))
        return MiniMessageImpl.parse(format, tags)
    }

    private fun groupToData(group: PrefixesGroup): PrefixesPlayerData {
        return PrefixesPlayerData(
            group.getPrefix() ?: Component.empty(),
            group.getSuffix() ?: Component.empty(),
            group.getColor() ?: NamedTextColor.WHITE,
            group.getPriority()
        )
    }

    private fun applyJoinName(player: Player) {
        val group = getHighestGroup(player)
        Bukkit.getScheduler().runTask(plugin, Runnable {
            val data = groupToData(group)
            val prefixData = PrefixesConfigureEvent(
                player, data
            )
            Bukkit.getPluginManager().callEvent(prefixData)
            setWholeName(
                player, data
            )
            Bukkit.getPluginManager().callEvent(PrefixesConfiguredEvent(player))
        })
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        applyJoinName(event.player)
        name.forEntity(event.player).setName { viewer ->
            val usedDisplay = display.getDisplay(viewer)
                .orElseGet { display.getDefaultDisplay() }
            val data =
                usedDisplay.getCurrent(event.player) ?: display.getDefaultDisplay()?.getCurrent(event.player)
            return@setName data?.toFormattedName(event.player.name())
                ?: event.player.name()
        }
    }

    // TODO: Find a better event to listen to, we want to register the audience directly after all online player infos have loaded, for now this is one tick delayed
    @EventHandler
    fun onPacketReady(event: PlayerClientLoadedWorldEvent) {
        if (hasAudience(event.player)) return
        registerAudience(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        removeAudience(event.player)
        display.remove(event.player)
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        event.renderer { player, _, message, viewer ->
            return@renderer formatChatMessage(player, config.getChatFormat(), message, viewer)
        }
    }
}

private fun Audience.toSingleAudiences(): List<Audience> {
    val uuid = this.get(Identity.UUID)
    if (uuid.isPresent) {
        return listOf(this)
    }
    val result = mutableListOf<Audience>()
    this.forEachAudience { if (it != this) result.addAll(it.toSingleAudiences()) }
    return result
}