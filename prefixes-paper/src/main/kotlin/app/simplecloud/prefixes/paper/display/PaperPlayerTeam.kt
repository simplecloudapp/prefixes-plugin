package app.simplecloud.prefixes.paper.display

import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import app.simplecloud.prefixes.shared.utilities.getPriorityString
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.ChatFormatting
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import org.bukkit.entity.Player

class PaperPlayerTeam(player: Player, content: PrefixesPlayerData) : PlayerTeam(Scoreboard(), "${content.getPriorityString()}_${player.name}") {

    init {
        players.add(player.name)
        nameTagVisibility = Visibility.NEVER
        color = ChatFormatting.valueOf(NamedTextColor.nearestTo(content.color).toString().uppercase())

        setPlayerPrefix(PaperAdventure.asVanilla(content.prefix))
        setPlayerSuffix(PaperAdventure.asVanilla(content.suffix))
    }
}
