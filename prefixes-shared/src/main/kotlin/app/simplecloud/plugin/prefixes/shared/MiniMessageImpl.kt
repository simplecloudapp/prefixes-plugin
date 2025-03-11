package app.simplecloud.plugin.prefixes.shared

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class MiniMessageImpl {
    companion object {
        private val miniMessage = MiniMessage.miniMessage()
        fun parse(text: String): Component {
            if (text.contains("§")) {
                return ComponentSerializerImpl.deserializeLegacy(text)
            }
            return miniMessage.deserialize(text)
        }

        fun parse(text: String, vararg tags: TagResolver): Component {
            if (text.contains("§")) {
                return ComponentSerializerImpl.deserializeLegacy(text)
            }
            return miniMessage.deserialize(text, TagResolver.resolver(tags.asIterable()))
        }

        fun parse(text: String, tags: Iterable<TagResolver>): Component {
            if (text.contains("§")) {
                return ComponentSerializerImpl.deserializeLegacy(text)
            }
            return miniMessage.deserialize(text, TagResolver.resolver(tags))
        }
    }
}