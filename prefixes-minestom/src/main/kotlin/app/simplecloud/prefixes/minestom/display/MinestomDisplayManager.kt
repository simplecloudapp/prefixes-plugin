package app.simplecloud.prefixes.minestom.display

import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import app.simplecloud.prefixes.minestom.permission.MinestomPermissions
import app.simplecloud.prefixes.shared.Prefixes
import app.simplecloud.prefixes.shared.config.CONFIG_SOURCE
import app.simplecloud.prefixes.shared.config.FeaturesConfig
import app.simplecloud.prefixes.shared.sync.tablist.ProfileProperty
import app.simplecloud.prefixes.shared.sync.tablist.TablistEntry
import app.simplecloud.prefixes.shared.sync.tablist.TablistGameMode
import app.simplecloud.prefixes.shared.utilities.PlayerDisplayFormatter
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.scoreboard.Team
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level
import java.util.logging.Logger

class MinestomDisplayManager(
    private val prefixes: Prefixes,
    private val permissions: MinestomPermissions
) {

    private val logger = Logger.getLogger("simplecloud-prefixes")
    private val cache = ConcurrentHashMap<UUID, PrefixesPlayerData>()
    private val teams = ConcurrentHashMap<UUID, Team>()
    private val nameTags = ConcurrentHashMap<UUID, MinestomNameTag>()
    private val entries = ConcurrentHashMap<UUID, TablistEntry>()
    private val warned = AtomicBoolean()

    fun getPlayer(id: UUID): PrefixesPlayerData? {
        return cache[id]
    }

    fun addPlayer(player: Player) {
        warnMissingPermissionHandler()
        updatePlayer(player)
    }

    fun updatePlayer(player: Player) {
        prefixes.api.getPrefixData(player.uuid).thenAccept { data ->
            applyPrefixData(player, data)
        }.exceptionally { throwable ->
            logger.log(Level.WARNING, "Failed to update prefix data of ${player.username}", throwable)
            null
        }
    }

    fun removePlayer(player: Player) {
        cache.remove(player.uuid)
        removeTeam(player.uuid)
        removeNameTag(player.uuid)

        entries.remove(player.uuid)
        prefixes.sync?.publisher?.publishTablistRemove(player.uuid)
    }

    fun setSneaking(player: Player, sneaking: Boolean) {
        nameTags[player.uuid]?.setSneaking(sneaking)
    }

    fun refreshNameTag(player: Player) {
        if (removeNameTag(player.uuid) == null) return

        val data = cache[player.uuid] ?: return
        val features = prefixes.config.get().features
        updateNameTag(player, displayName(data, player, features), features)
    }

    fun clear() {
        cache.clear()
        entries.clear()
        teams.keys.toList().forEach(::removeTeam)
        nameTags.keys.toList().forEach(::removeNameTag)
    }

    /**
     * Publishes the tab list entries of all players to the network.
     */
    fun sync(force: Boolean = false) {
        MinecraftServer.getConnectionManager().onlinePlayers.forEach { player ->
            val data = cache[player.uuid] ?: return@forEach
            val features = prefixes.config.get().features

            publish(player, data, displayName(data, player, features), force)
        }
    }

    private fun applyPrefixData(player: Player, data: PrefixesPlayerData) {
        if (!player.isOnline) return
        cache[player.uuid] = data

        val features = prefixes.config.get().features
        val displayName = displayName(data, player, features)

        player.displayName = when {
            features.tablist -> PlayerDisplayFormatter.formatTablistName(data, displayName)
            else -> null
        }

        updateTeam(player, data, features)
        updateNameTag(player, displayName, features)

        publish(player, data, displayName)
    }

    private fun updateTeam(player: Player, data: PrefixesPlayerData, features: FeaturesConfig) {
        removeTeam(player.uuid)

        val team = createTeam(player, data, features) ?: return
        teams[player.uuid] = team
    }

    private fun createTeam(player: Player, data: PrefixesPlayerData, features: FeaturesConfig): Team? {
        return when {
            features.tablist -> MinestomPlayerTeam.create(
                player.username,
                data.priority,
                data.prefix,
                data.suffix,
                data.color,
                hideNameTag = features.displayName
            )

            // Only there to hide the vanilla name tag, which the name tag entity replaces.
            features.displayName -> MinestomPlayerTeam.create(player.username, priority = 0, hideNameTag = true)
            else -> null
        }
    }

    private fun removeTeam(id: UUID) {
        val team = teams.remove(id) ?: return
        MinestomPlayerTeam.delete(team)
    }

    private fun updateNameTag(player: Player, displayName: Component, features: FeaturesConfig) {
        if (!features.displayName) {
            removeNameTag(player.uuid)
            return
        }

        val nameTag = nameTags.computeIfAbsent(player.uuid) {
            MinestomNameTag(player).also(MinestomNameTag::spawn)
        }

        nameTag.setName(displayName)
    }

    private fun removeNameTag(id: UUID): MinestomNameTag? {
        return nameTags.remove(id)?.also(MinestomNameTag::remove)
    }

    private fun publish(player: Player, data: PrefixesPlayerData, displayName: Component, force: Boolean = false) {
        val config = prefixes.config.get()
        if (!config.features.tablist || !config.sync.enabled || !config.sync.channels.tablist) return
        val publisher = prefixes.sync?.publisher ?: return

        val entry = TablistEntry(
            uniqueId = player.uuid,
            name = player.username,
            displayName = PlayerDisplayFormatter.formatTablistName(data, displayName),
            priority = data.priority,
            profileProperties = getProfileProperties(player),
            latency = player.latency,
            gameMode = TablistGameMode.valueOf(player.gameMode.name),
            showHat = (player.settings.displayedSkinParts.toInt() and 0x40) != 0,
            listOrder = player.listOrder
        )
        val previous = entries.put(player.uuid, entry)
        if (!force && previous == entry) return

        publisher.publishTablistEntry(entry)
    }

    private fun getProfileProperties(player: Player): List<ProfileProperty> {
        val skin = player.skin ?: return emptyList()
        return listOf(ProfileProperty("textures", skin.textures(), skin.signature()))
    }

    private fun displayName(data: PrefixesPlayerData, player: Player, features: FeaturesConfig): Component {
        return PlayerDisplayFormatter.displayName(data, player.username, features.displayName)
    }

    private fun warnMissingPermissionHandler() {
        if (permissions.hasHandler || !warned.compareAndSet(false, true)) return
        if (!prefixes.registry.getCurrentGroupProvider().getName().equals(CONFIG_SOURCE, ignoreCase = true)) return

        logger.warning(
            "Source Type is set to $CONFIG_SOURCE, but no permission handler was set! " +
            "Every player will receive the default group. " +
            "Pass one to PrefixesMinestom.builder(...).permissionHandler(...) or register your own group provider."
        )
    }

}
