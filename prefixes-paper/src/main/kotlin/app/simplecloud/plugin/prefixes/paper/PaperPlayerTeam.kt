package app.simplecloud.plugin.prefixes.paper

import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.minecraft.ChatFormatting
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard

class PaperPlayerTeam(id: String, val priority: Int = 0) :
    PlayerTeam(Scoreboard(), "${toPriorityString(priority)}${id}") {

    init {
        nameTagVisibility = Visibility.NEVER
    }

    var realColor: TextColor = NamedTextColor.WHITE
    set(value) {
        field = value
        color = ChatFormatting.valueOf(NamedTextColor.nearestTo(value).toString().uppercase())
    }

    fun getFormattedName(formattedName: Component): Component {
        val mutableComponent = Component.empty().append(PaperAdventure.asAdventure(this.playerPrefix))
            .append(formattedName.color(realColor)).append(
                PaperAdventure.asAdventure(this.playerSuffix)
            )
        return mutableComponent
    }
}

private fun toPriorityString(priority: Int): String {
    if (priority < 0) return "000"
    if (priority > 999) return "999"
    var result = priority.toString()
    for (i in 0 until 3 - result.length) {
        result = "0${result}"
    }
    return result
}