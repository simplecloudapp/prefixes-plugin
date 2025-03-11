package app.simplecloud.plugin.prefixes.api.impl

import app.simplecloud.plugin.prefixes.api.PrefixesActor
import app.simplecloud.plugin.prefixes.api.PrefixesApi
import app.simplecloud.plugin.prefixes.api.PrefixesGroup
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.*

class PrefixesActorBlankImpl : PrefixesActor {
    override fun registerViewer(target: UUID, api: PrefixesApi) {
        throw NotImplementedError("You need to define a PrefixesActor to use this")
    }

    override fun hasViewer(target: UUID): Boolean {
        throw NotImplementedError("You need to define a PrefixesActor to use this")
    }

    override fun removeViewer(target: UUID) {
        throw NotImplementedError("You need to define a PrefixesActor to use this")
    }

    override fun applyGroup(target: UUID, group: PrefixesGroup, vararg viewers: UUID) {
        throw NotImplementedError("You need to define a PrefixesActor to use this")
    }

    override fun setPrefix(target: UUID, prefix: Component, vararg viewers: UUID) {
        throw NotImplementedError("You need to define a PrefixesActor to use this")
    }

    override fun setSuffix(target: UUID, suffix: Component, vararg viewers: UUID) {
        throw NotImplementedError("You need to define a PrefixesActor to use this")
    }

    override fun setColor(target: UUID, color: TextColor, vararg viewers: UUID) {
        throw NotImplementedError("You need to define a PrefixesActor to use this")
    }

    override fun apply(target: UUID, prefix: Component, color: TextColor, suffix: Component, priority: Int, vararg viewers: UUID) {
        throw NotImplementedError("You need to define a PrefixesActor to use this")
    }

    override fun formatMessage(target: UUID, viewer: UUID?, format: String, message: Component): Component {
        throw NotImplementedError("You need to define a PrefixesActor to use this")
    }

    override fun remove(target: UUID) {
        throw NotImplementedError("You need to define a PrefixesActor to use this")
    }
}