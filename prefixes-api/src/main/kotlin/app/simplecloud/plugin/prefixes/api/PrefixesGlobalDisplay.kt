package app.simplecloud.plugin.prefixes.api

import net.kyori.adventure.text.format.TextColor
import java.util.*

open class PrefixesGlobalDisplay<C, P, T> {


    private val displays = mutableMapOf<UUID, PrefixesDisplay<C, P, T>>()


    private var defaultDisplay: PrefixesDisplay<C, P, T>? = null

    private fun executeFor(players: List<UUID>, action: (display: PrefixesDisplay<C, P, T>) -> Unit) {
        displays.filter { players.isEmpty() || players.contains(it.key) }.forEach { display ->
            action(display.value)
        }
        if (players.isEmpty())
            defaultDisplay?.let { action(it) }
    }

    fun getDisplay(player: UUID): Optional<PrefixesDisplay<C, P, T>> {
        return Optional.ofNullable(displays.getOrDefault(player, null))
    }

    fun removeDisplay(player: UUID) {
        displays.remove(player)
    }

    fun getDefaultDisplay(): PrefixesDisplay<C, P, T>? {
        return defaultDisplay
    }

    fun setDefaultDisplay(display: PrefixesDisplay<C, P, T>) {
        this.defaultDisplay = display
    }

    fun register(uuid: UUID, display: PrefixesDisplay<C, P, T>) {
        displays[uuid] = display
    }

    fun createTeam(id: String) {
        executeFor(displays.keys.toList()) {
            it.createTeam(id)
        }
    }

    fun updatePrefix(id: String, prefix: C, vararg players: UUID) {
        executeFor(players.toList()) {
            it.updatePrefix(id, prefix)
        }
    }

    fun updateSuffix(id: String, suffix: C, vararg players: UUID) {
        executeFor(players.toList()) {
            it.updateSuffix(id, suffix)
        }
    }

    fun updatePriority(id: String, priority: Int, vararg players: UUID) {
        executeFor(players.toList()) {
            it.updatePriority(id, priority)
        }
    }

    fun update(id: String, prefix: C, suffix: C, priority: Int, vararg players: UUID) {
        executeFor(players.toList()) {
            it.update(id, prefix, suffix, priority)
        }
    }

    open fun setPlayer(id: String, player: P, vararg players: UUID) {
        executeFor(players.toList()) {
            it.setPlayer(id, player)
        }
    }

    fun removePlayer(player: P, vararg players: UUID) {
        executeFor(players.toList()) {
            it.removePlayer(player)
        }
    }

    fun updateColor(id: String, color: TextColor, vararg players: UUID) {
        executeFor(players.toList()) {
            it.updateColor(id, color)
        }
    }

}