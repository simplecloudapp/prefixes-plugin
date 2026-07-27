package app.simplecloud.prefixes.shared.sync.tablist

import app.simplecloud.prefixes.shared.utilities.serializer
import app.simplecloud.prefixes.v1.TablistEntryUpdateEvent
import net.kyori.adventure.text.Component
import java.util.UUID

/**
 * Represents the tablist entry of a player on another server.
 */
data class TablistEntry(
    val uniqueId: UUID,
    val name: String,
    val displayName: Component,
    val priority: Int
) {

    fun toDefinition(): TablistEntryUpdateEvent = TablistEntryUpdateEvent.newBuilder()
        .setPlayerId(uniqueId.toString())
        .setPlayerName(name)
        .setDisplayName(serializer.serialize(displayName))
        .setPriority(priority)
        .build()

    companion object {
        fun fromDefinition(definition: TablistEntryUpdateEvent) = TablistEntry(
            uniqueId = UUID.fromString(definition.playerId),
            name = definition.playerName,
            displayName = serializer.deserialize(definition.displayName),
            priority = definition.priority
        )
    }
}