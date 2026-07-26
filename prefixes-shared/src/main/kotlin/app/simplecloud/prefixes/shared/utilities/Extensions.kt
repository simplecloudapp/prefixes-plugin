package app.simplecloud.prefixes.shared.utilities

import app.simplecloud.prefixes.api.group.PrefixesPlayerData

fun PrefixesPlayerData.getPriorityString(): String {
    val inverted = (1000 - priority).coerceIn(0, 999)
    return String.format("%03d", inverted)
}