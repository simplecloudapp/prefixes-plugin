package app.simplecloud.plugin.prefixes.api

import net.kyori.adventure.text.format.TextColor

interface PrefixesDisplay<C, P, T> {
    fun createTeam(id: String, priority: Int = 0): T?
    fun getTeam(id: String): T?
    fun updatePrefix(id: String, prefix: C)
    fun getPriority(team: T): Int?
    fun updateSuffix(id: String, suffix: C)

    fun updatePriority(id: String, priority: Int): T?
    fun updateColor(id: String, color: TextColor)
    fun update(id: String, prefix: C, suffix: C, priority: Int)

    fun toPriorityString(priority: Int): String {
        if (priority < 0) return "000"
        if (priority > 999) return "999"
        var result = priority.toString()
        for (i in 0 until 3 - result.length) {
            result = "0${result}"
        }
        return result
    }

    fun setPlayer(id: String, player: P)
    fun removePlayer(player: P)
    fun setViewer(player: P): Boolean
    fun addViewer(player: P): Boolean
    fun removeViewer(player: P): Boolean
    fun getViewers(): Set<P>
}