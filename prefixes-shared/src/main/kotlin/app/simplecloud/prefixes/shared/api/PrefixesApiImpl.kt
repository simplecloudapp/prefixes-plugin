package app.simplecloud.prefixes.shared.api

import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.prefixes.api.PrefixesApi
import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.api.group.PrefixesGroup
import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.util.UUID
import java.util.concurrent.CompletableFuture

class PrefixesApiImpl(
    private val provider: () -> GroupProvider,
    private val playerResolver: (UUID) -> String
) : PrefixesApi {

    override fun getGroups(): Collection<PrefixesGroup> = provider().getGroups()

    override fun getPrimaryGroup(id: UUID): CompletableFuture<PrefixesGroup?> = provider().getGroup(id)

    override fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean> = provider().addGroup(group)

    override fun getPrefixData(id: UUID): CompletableFuture<PrefixesPlayerData> {
        return getPrimaryGroup(id).thenApply { group ->
            val prefix = group?.prefix ?: Component.empty()
            val suffix = group?.suffix ?: Component.empty()
            val color = group?.color ?: NamedTextColor.WHITE
            val priority = group?.priority ?: 0
            val displayName = group?.displayName ?: "<color><playername>"
            val chatFormat = group?.chatFormat ?: "<playername> <message>"

            val player = playerResolver(id)
            val display = miniMessage.deserialize(
                displayName,
                Placeholder.styling("color", color),
                Placeholder.unparsed("playername", player)
            )

            PrefixesPlayerData(prefix, suffix, color, display, chatFormat, priority)

        }

    }

}