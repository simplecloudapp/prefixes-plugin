package app.simplecloud.prefixes.shared.sync.tablist

import app.simplecloud.prefixes.shared.utilities.ComponentSerialization
import app.simplecloud.prefixes.v1.TablistEntryUpdateEvent
import app.simplecloud.prefixes.v1.TablistGameMode as TablistGameModeDefinition
import app.simplecloud.prefixes.v1.TablistProfileProperty
import java.util.UUID

object TablistEntryMapper {

    fun toDefinition(entry: TablistEntry): TablistEntryUpdateEvent =
        TablistEntryUpdateEvent.newBuilder()
            .setPlayerId(entry.uniqueId.toString())
            .setPlayerName(entry.name)
            .setDisplayName(ComponentSerialization.serialize(entry.displayName))
            .setPriority(entry.priority)
            .addAllProfileProperties(entry.profileProperties.map(::toDefinition))
            .setLatency(entry.latency)
            .setGameMode(toDefinition(entry.gameMode))
            .setShowHat(entry.showHat)
            .setListOrder(entry.listOrder)
            .build()

    fun fromDefinition(definition: TablistEntryUpdateEvent) = TablistEntry(
        uniqueId = UUID.fromString(definition.playerId),
        name = definition.playerName,
        displayName = ComponentSerialization.deserialize(definition.displayName),
        priority = definition.priority,
        profileProperties = definition.profilePropertiesList.map(::fromDefinition),
        latency = definition.latency,
        gameMode = fromDefinition(definition.gameMode),
        showHat = definition.showHat.takeIf { definition.hasShowHat() } ?: true,
        listOrder = definition.listOrder
    )

    private fun toDefinition(property: ProfileProperty): TablistProfileProperty {
        val definition = TablistProfileProperty.newBuilder()
            .setName(property.name)
            .setValue(property.value)
        property.signature?.let(definition::setSignature)
        return definition.build()
    }

    private fun fromDefinition(property: TablistProfileProperty) = ProfileProperty(
        name = property.name,
        value = property.value,
        signature = property.signature.takeIf { property.hasSignature() }
    )

    private fun toDefinition(gameMode: TablistGameMode): TablistGameModeDefinition = when (gameMode) {
        TablistGameMode.SURVIVAL -> TablistGameModeDefinition.TABLIST_GAME_MODE_SURVIVAL
        TablistGameMode.CREATIVE -> TablistGameModeDefinition.TABLIST_GAME_MODE_CREATIVE
        TablistGameMode.ADVENTURE -> TablistGameModeDefinition.TABLIST_GAME_MODE_ADVENTURE
        TablistGameMode.SPECTATOR -> TablistGameModeDefinition.TABLIST_GAME_MODE_SPECTATOR
    }

    private fun fromDefinition(definition: TablistGameModeDefinition): TablistGameMode =
        when (definition) {
            TablistGameModeDefinition.TABLIST_GAME_MODE_CREATIVE -> TablistGameMode.CREATIVE
            TablistGameModeDefinition.TABLIST_GAME_MODE_ADVENTURE -> TablistGameMode.ADVENTURE
            TablistGameModeDefinition.TABLIST_GAME_MODE_SPECTATOR -> TablistGameMode.SPECTATOR
            else -> TablistGameMode.SURVIVAL
        }
}
