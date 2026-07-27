package app.simplecloud.prefixes.shared.utilities

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

object ComponentSerialization {

    private val serializer = GsonComponentSerializer.gson()

    fun serialize(component: Component): String {
        return serializer.serialize(component)
    }

    fun deserialize(json: String): Component {
        return serializer.deserialize(json)
    }
}
