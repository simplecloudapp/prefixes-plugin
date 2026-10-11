package app.simplecloud.prefixes.shared.utilities

object PriorityFormatter {

    fun format(priority: Int): String {
        val inverted = Int.MAX_VALUE.toLong() - priority
        return inverted.toString().padStart(10, '0')
    }
}
