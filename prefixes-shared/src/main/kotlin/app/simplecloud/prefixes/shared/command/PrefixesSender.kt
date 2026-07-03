package app.simplecloud.prefixes.shared.command

import net.kyori.adventure.text.Component

interface PrefixesSender {

    fun sendMessage(message: Component)

    fun hasPermission(permission: String): Boolean

}