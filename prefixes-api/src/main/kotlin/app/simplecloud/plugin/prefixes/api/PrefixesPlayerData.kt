package app.simplecloud.plugin.prefixes.api

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.jetbrains.annotations.ApiStatus

data class PrefixesPlayerData(
    var prefix: Component,
    var suffix: Component,
    var color: TextColor,
    @ApiStatus.Internal
    var priority: Int,
) {
    fun toFormattedName(name: Component): Component {
        val mutableComponent = Component.empty().append(prefix)
            .append(name.color(color)).append(
                suffix
            )
        return mutableComponent
    }
}
