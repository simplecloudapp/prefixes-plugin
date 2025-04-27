package app.simplecloud.plugin.prefixes.paper.impl

import app.simplecloud.plugin.prefixes.api.PrefixesGlobalDisplay
import net.kyori.adventure.audience.ForwardingAudience
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PaperPrefixesGlobalDisplayImpl :
    PrefixesGlobalDisplay<Player>() {
    init {
        setDefaultDisplay(PaperPrefixesDisplayImpl(ForwardingAudience { Bukkit.getOnlinePlayers() }))
    }
}