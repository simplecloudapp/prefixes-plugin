package app.simplecloud.plugin.prefixes.api

import net.kyori.adventure.audience.Audience
import java.util.*

open class PrefixesGlobalDisplay<P>() {


    private val displays = mutableMapOf<Audience, PrefixesDisplay<P>>()


    private var defaultDisplay: PrefixesDisplay<P>? = null

    private fun executeFor(audience: Audience? = null, action: (display: PrefixesDisplay<P>) -> Unit) {
        if (audience == null) {
            defaultDisplay?.let { action(it) }
            return
        }
        displays.filter { it.key == audience }.forEach { display ->
            action(display.value)
        }
    }

    fun getDisplay(audience: Audience): Optional<PrefixesDisplay<P>> {
        return Optional.ofNullable(displays.getOrDefault(audience, null))
    }

    fun remove(audience: Audience) {
        displays.remove(audience)
    }

    fun getDefaultDisplay(): PrefixesDisplay<P>? {
        return defaultDisplay
    }

    fun setDefaultDisplay(display: PrefixesDisplay<P>) {
        this.defaultDisplay = display
    }

    fun register(audience: Audience, display: PrefixesDisplay<P>) {
        displays[audience] = display
        defaultDisplay?.getAll()?.forEach { display.apply(it.key, it.value) }
    }

    fun apply(player: P, content: PrefixesPlayerData, audience: Audience?) {
        // Remove the player from all other audiences as we change something globally
        if (audience == null) {
            displays.filter { it.key != defaultDisplay }.forEach { it.value.remove(player) }
        }

        executeFor(audience) {
            // Transition from the global display if we need to
            if (it != defaultDisplay && it.getCurrent(player) == null) {
                val present = defaultDisplay?.getCurrent(player)
                if (present != null) {
                    it.transition(player, present)
                }
            }
            it.apply(player, content)
        }
    }

    fun remove(player: P) {
        defaultDisplay?.remove(player)
        displays.forEach { it.value.remove(player) }
    }

    fun getCurrent(player: P, audience: Audience?): PrefixesPlayerData? {
        var result: PrefixesPlayerData? = null
        executeFor(audience) { result = it.getCurrent(player) }
        return result
    }

}