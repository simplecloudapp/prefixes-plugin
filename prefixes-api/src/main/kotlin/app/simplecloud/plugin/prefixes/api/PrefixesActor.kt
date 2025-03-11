package app.simplecloud.plugin.prefixes.api

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.*

interface PrefixesActor {
    fun registerViewer(target: UUID, api: PrefixesApi)
    fun hasViewer(target: UUID): Boolean
    fun removeViewer(target: UUID)
    fun applyGroup(target: UUID, group: PrefixesGroup, vararg viewers: UUID)
    fun setPrefix(target: UUID, prefix: Component, vararg viewers: UUID)
    fun setSuffix(target: UUID, suffix: Component, vararg viewers: UUID)
    fun setColor(target: UUID, color: TextColor, vararg viewers: UUID)
    fun apply(target: UUID, prefix: Component, color: TextColor, suffix: Component, priority: Int, vararg viewers: UUID)
    fun formatMessage(target: UUID, viewer: UUID?, format: String, message: Component): Component
    fun remove(target: UUID)
}