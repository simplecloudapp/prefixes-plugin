package app.simplecloud.plugin.prefixes.paper

import app.simplecloud.plugin.prefixes.api.PrefixesPlayerData
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.ChatFormatting
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import org.bukkit.entity.Player

class PaperPlayerTeam(player: Player, content: PrefixesPlayerData) :
    PlayerTeam(Scoreboard(), "${content.getPriorityString()}${player.name}") {

    init {
        nameTagVisibility = Visibility.NEVER
        color = ChatFormatting.valueOf(NamedTextColor.nearestTo(content.color).toString().uppercase())
        playerPrefix = PaperAdventure.asVanilla(content.prefix)
        playerSuffix = PaperAdventure.asVanilla(content.suffix)
    }
}

private fun PrefixesPlayerData.getPriorityString(): String {
    if (priority < 0) return "000"
    if (priority > 999) return "999"
    var result = priority.toString()
    for (i in 0 until 3 - result.length) {
        result = "0${result}"
    }
    return result
}