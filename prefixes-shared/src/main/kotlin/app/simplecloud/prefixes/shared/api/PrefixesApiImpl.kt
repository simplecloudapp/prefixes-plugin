package app.simplecloud.prefixes.shared.api

import app.simplecloud.plugin.api.shared.extension.miniMessage
import app.simplecloud.prefixes.api.PrefixesApi
import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.api.group.PrefixesGroup
import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import app.simplecloud.prefixes.shared.group.GroupProviders
import app.simplecloud.prefixes.shared.platform.PrefixesListener
import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.util.UUID
import java.util.concurrent.CompletableFuture

class PrefixesApiImpl(
    private val providers: GroupProviders,
    private val listeners: List<PrefixesListener>,
    private val platform: PrefixesPlatform
) : PrefixesApi {

    override fun getGroups(): CompletableFuture<Collection<PrefixesGroup>> = providers.current().getGroups()

    override fun getPrimaryGroup(id: UUID): CompletableFuture<PrefixesGroup?> = providers.current().getGroup(id)

    override fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean> = providers.current().addGroup(group)

    override fun registerGroupProvider(provider: GroupProvider) = providers.register(provider)

    override fun unregisterGroupProvider(name: String) = providers.unregister(name)

    override fun getGroupProvider(): GroupProvider = providers.current()

    override fun getGroupProviders(): Collection<GroupProvider> = providers.all()

    override fun refreshPlayer(id: UUID) {
        listeners.forEach { listener -> listener.onPlayerUpdate(id) }
    }

    override fun refreshAll() {
        listeners.forEach(PrefixesListener::onAllPlayersUpdate)
    }

    override fun getPrefixData(id: UUID): CompletableFuture<PrefixesPlayerData> {
        return getPrimaryGroup(id).thenApply { group -> createPlayerData(id, group) }
    }

    private fun createPlayerData(id: UUID, group: PrefixesGroup?): PrefixesPlayerData {
        val color = group?.color ?: NamedTextColor.WHITE
        val displayName = miniMessage.deserialize(
            group?.displayName ?: "<color><playername>",
            Placeholder.styling("color", color),
            Placeholder.unparsed("playername", platform.getPlayerName(id))
        )

        return PrefixesPlayerData(
            group?.prefix ?: Component.empty(),
            group?.suffix ?: Component.empty(),
            color,
            displayName,
            group?.chatFormat ?: "<playername> <message>",
            group?.priority ?: 0
        )
    }
}
