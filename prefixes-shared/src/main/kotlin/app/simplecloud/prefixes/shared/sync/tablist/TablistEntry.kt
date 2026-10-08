package app.simplecloud.prefixes.shared.sync.tablist

import net.kyori.adventure.text.Component
import java.util.UUID

data class TablistEntry(
    val uniqueId: UUID,
    val name: String,
    val displayName: Component,
    val priority: Int,
    val profileProperties: List<ProfileProperty> = emptyList(),
    val latency: Int = 0,
    val gameMode: TablistGameMode = TablistGameMode.SURVIVAL,
    val showHat: Boolean = true,
    val listOrder: Int = 0
)
