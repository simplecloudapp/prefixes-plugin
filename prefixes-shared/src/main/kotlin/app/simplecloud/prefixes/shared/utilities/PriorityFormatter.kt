package app.simplecloud.prefixes.shared.utilities

object PriorityFormatter {

    fun format(priority: Int): String {
        val inverted = (1000 - priority).coerceIn(0, 999)
        return String.format("%03d", inverted)
    }
}
