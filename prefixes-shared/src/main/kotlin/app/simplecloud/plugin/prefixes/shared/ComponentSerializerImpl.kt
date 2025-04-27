package app.simplecloud.plugin.prefixes.shared

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class ComponentSerializerImpl {
    companion object {
        private val legacyImpl =
            LegacyComponentSerializer.builder().hexColors().character('§').useUnusualXRepeatedCharacterHexFormat()
                .hexCharacter('x').build()

        fun deserializeLegacy(text: String): Component {
            return legacyImpl.deserialize(text)
        }
    }
}