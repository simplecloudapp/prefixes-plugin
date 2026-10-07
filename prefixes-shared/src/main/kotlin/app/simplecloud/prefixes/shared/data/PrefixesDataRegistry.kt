package app.simplecloud.prefixes.shared.data

import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.prefixes.api.group.PrefixesGroup
import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
import app.simplecloud.prefixes.shared.utilities.AudienceResolver
import app.simplecloud.prefixes.shared.PrefixesConstants
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PrefixesDataRegistry(private val platform: PrefixesPlatform) {

    private val groups = ConcurrentHashMap<UUID, PrefixesOverride>()
    private val overrides = ConcurrentHashMap<UUID, PrefixesOverride>()
    private val viewerOverrides = ConcurrentHashMap<ViewerKey, PrefixesOverride>()

    fun isLoaded(id: UUID): Boolean = groups.containsKey(id)

    fun getData(id: UUID): PrefixesPlayerData? {
        val group = groups[id] ?: return null
        return createData(id, group.merge(overrides[id]))
    }

    fun getData(id: UUID, group: PrefixesGroup?): PrefixesPlayerData {
        return createData(id, PrefixesOverride.of(group).merge(overrides[id]))
    }

    fun getData(id: UUID, group: PrefixesGroup?, viewer: UUID): PrefixesPlayerData {
        return createData(id, PrefixesOverride.of(group).merge(overrides[id]).merge(viewerOverrides[ViewerKey(id, viewer)]))
    }

    fun getViewerData(id: UUID, viewer: UUID): PrefixesPlayerData? {
        val group = groups[id] ?: return null
        val viewerOverride = viewerOverrides[ViewerKey(id, viewer)] ?: return null
        return createData(id, group.merge(overrides[id]).merge(viewerOverride))
    }

    fun getViewerData(id: UUID, viewer: Audience): PrefixesPlayerData? {
        val viewerId = AudienceResolver.resolveId(viewer) ?: return null
        return getViewerData(id, viewerId)
    }

    fun loadGroup(id: UUID, group: PrefixesGroup?) {
        groups[id] = PrefixesOverride.of(group)
    }

    fun set(id: UUID, override: PrefixesOverride) {
        overrides.merge(id, override) { current, new -> current.merge(new) }
    }

    fun set(id: UUID, viewer: UUID, override: PrefixesOverride) {
        viewerOverrides.merge(ViewerKey(id, viewer), override) { current, new -> current.merge(new) }
    }

    fun reset(id: UUID) {
        overrides.remove(id)
        viewerOverrides.keys.removeIf { key -> key.target == id }
    }

    fun reset(id: UUID, viewer: UUID) {
        viewerOverrides.remove(ViewerKey(id, viewer))
    }

    fun remove(id: UUID) {
        groups.remove(id)
        overrides.remove(id)
        viewerOverrides.keys.removeIf { key -> key.target == id || key.viewer == id }
    }

    private fun createData(id: UUID, override: PrefixesOverride): PrefixesPlayerData {
        val prefix = override.prefix ?: Component.empty()
        val suffix = override.suffix ?: Component.empty()
        val color = override.color ?: NamedTextColor.WHITE
        val displayName = miniMessage.deserialize(
            override.displayName ?: PrefixesConstants.DEFAULT_DISPLAY_NAME,
            Placeholder.component("prefix", prefix),
            Placeholder.component("suffix", suffix),
            Placeholder.styling("color", color),
            Placeholder.unparsed("playername", platform.getPlayerName(id))
        )

        return PrefixesPlayerData(
            prefix,
            suffix,
            color,
            displayName,
            override.chatFormat ?: PrefixesConstants.DEFAULT_CHAT_FORMAT,
            override.priority ?: 0
        )
    }
}
