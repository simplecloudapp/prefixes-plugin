package app.simplecloud.prefixes.shared.utilities

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

object ComponentSerializer {

    private val serializer = GsonComponentSerializer.gson()

    fun serialize(component: Component): String = serializer.serialize(component)

    fun deserialize(json: String): Component = serializer.deserialize(json)
}
