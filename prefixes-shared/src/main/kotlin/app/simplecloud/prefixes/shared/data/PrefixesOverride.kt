package app.simplecloud.prefixes.shared.data

import app.simplecloud.prefixes.api.group.PrefixesGroup
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

data class PrefixesOverride(
    val prefix: Component? = null,
    val suffix: Component? = null,
    val color: TextColor? = null,
    val priority: Int? = null,
    val displayName: String? = null,
    val chatFormat: String? = null
) {

    fun merge(other: PrefixesOverride?): PrefixesOverride {
        if (other == null) return this

        return PrefixesOverride(
            other.prefix ?: prefix,
            other.suffix ?: suffix,
            other.color ?: color,
            other.priority ?: priority,
            other.displayName ?: displayName,
            other.chatFormat ?: chatFormat
        )
    }

    companion object {
        fun of(group: PrefixesGroup?): PrefixesOverride {
            if (group == null) return PrefixesOverride()

            return PrefixesOverride(
                group.prefix,
                group.suffix,
                group.color,
                group.priority,
                group.displayName.ifBlank { null },
                group.chatFormat.ifBlank { null }
            )
        }
    }
}
