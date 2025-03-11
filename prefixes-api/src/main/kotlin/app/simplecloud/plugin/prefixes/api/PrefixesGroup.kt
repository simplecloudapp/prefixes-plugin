package app.simplecloud.plugin.prefixes.api

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.*
import java.util.concurrent.CompletableFuture

interface PrefixesGroup {
    fun getName(): String
    fun getPrefix(): Component?
    fun getColor(): TextColor?
    fun getSuffix(): Component?
    fun getPriority(): Int
    fun containsPlayer(uniqueId: UUID): Boolean
    fun containsPlayerFuture(uniqueId: UUID): CompletableFuture<Boolean>
}