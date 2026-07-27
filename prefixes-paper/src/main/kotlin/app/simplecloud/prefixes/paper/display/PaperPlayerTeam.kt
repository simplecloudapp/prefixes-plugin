package app.simplecloud.prefixes.paper.display

import app.simplecloud.prefixes.shared.utilities.getPriorityString
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.TeamColor
import java.util.Optional

class PaperPlayerTeam(
    name: String,
    priority: Int,
    prefix: Component = Component.empty(),
    suffix: Component = Component.empty(),
    color: TextColor = NamedTextColor.WHITE
) : PlayerTeam(Scoreboard(), "${priority.getPriorityString()}_$name") {

    init {
        players.add(name)
        nameTagVisibility = Visibility.NEVER
        this.color = Optional.of(TeamColor.valueOf(NamedTextColor.nearestTo(color).toString().uppercase()))

        setPlayerPrefix(PaperAdventure.asVanilla(prefix))
        setPlayerSuffix(PaperAdventure.asVanilla(suffix))
    }
}
