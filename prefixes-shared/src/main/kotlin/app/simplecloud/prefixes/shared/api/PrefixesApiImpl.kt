package app.simplecloud.prefixes.shared.api

import app.simplecloud.prefixes.api.PrefixesApi
import app.simplecloud.prefixes.api.group.GroupProvider
import app.simplecloud.prefixes.api.group.PrefixesGroup
import app.simplecloud.prefixes.api.group.PrefixesPlayerData
import app.simplecloud.prefixes.shared.data.PrefixesDataRegistry
import app.simplecloud.prefixes.shared.data.PrefixesOverride
import app.simplecloud.prefixes.shared.group.GroupProviderRegistry
import app.simplecloud.prefixes.shared.platform.PrefixesListener
import app.simplecloud.prefixes.shared.platform.PrefixesPlatform
import app.simplecloud.prefixes.shared.utilities.AudienceResolver
import app.simplecloud.prefixes.shared.PrefixesConstants
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.UUID
import java.util.concurrent.CompletableFuture

class PrefixesApiImpl(
    private val registry: GroupProviderRegistry,
    private val dataRegistry: PrefixesDataRegistry,
    private val listeners: List<PrefixesListener>,
    private val platform: PrefixesPlatform
) : PrefixesApi {

    override fun getGroups(): CompletableFuture<Collection<PrefixesGroup>> = registry.getCurrentGroupProvider().getGroups()

    override fun getPrimaryGroup(id: UUID): CompletableFuture<PrefixesGroup?> = registry.getCurrentGroupProvider().getGroup(id)

    override fun getPrefixData(id: UUID): CompletableFuture<PrefixesPlayerData> {
        return PrefixesConstants.SCOPE.future { dataRegistry.getData(id, getPrimaryGroup(id).await()) }
    }

    override fun getPrefixData(id: UUID, viewer: Audience): CompletableFuture<PrefixesPlayerData> {
        val viewerId = AudienceResolver.resolveId(viewer) ?: return getPrefixData(id)
        return PrefixesConstants.SCOPE.future { dataRegistry.getData(id, getPrimaryGroup(id).await(), viewerId) }
    }

    override fun setPrefix(id: UUID, prefix: Component) = set(id, PrefixesOverride(prefix = prefix))

    override fun setPrefix(id: UUID, prefix: Component, viewers: Audience) = set(id, PrefixesOverride(prefix = prefix), viewers)

    override fun setSuffix(id: UUID, suffix: Component) = set(id, PrefixesOverride(suffix = suffix))

    override fun setSuffix(id: UUID, suffix: Component, viewers: Audience) = set(id, PrefixesOverride(suffix = suffix), viewers)

    override fun setColor(id: UUID, color: TextColor) = set(id, PrefixesOverride(color = color))

    override fun setColor(id: UUID, color: TextColor, viewers: Audience) = set(id, PrefixesOverride(color = color), viewers)

    override fun setPriority(id: UUID, priority: Int) = set(id, PrefixesOverride(priority = priority))

    override fun setPriority(id: UUID, priority: Int, viewers: Audience) = set(id, PrefixesOverride(priority = priority), viewers)

    override fun setGroup(id: UUID, group: String): CompletableFuture<Boolean> {
        return PrefixesConstants.SCOPE.future {
            val found = findGroup(group) ?: return@future false
            setGroup(id, found)
            true
        }
    }

    override fun setGroup(id: UUID, group: String, viewers: Audience): CompletableFuture<Boolean> {
        return PrefixesConstants.SCOPE.future {
            val found = findGroup(group) ?: return@future false
            setGroup(id, found, viewers)
            true
        }
    }

    override fun setGroup(targets: Audience, group: String): CompletableFuture<Boolean> {
        return PrefixesConstants.SCOPE.future {
            val found = findGroup(group) ?: return@future false
            setGroup(targets, found)
            true
        }
    }

    override fun setGroup(targets: Audience, group: String, viewers: Audience): CompletableFuture<Boolean> {
        return PrefixesConstants.SCOPE.future {
            val found = findGroup(group) ?: return@future false
            setGroup(targets, found, viewers)
            true
        }
    }

    override fun setGroup(id: UUID, group: PrefixesGroup) = set(id, PrefixesOverride.of(group))

    override fun setGroup(id: UUID, group: PrefixesGroup, viewers: Audience) = set(id, PrefixesOverride.of(group), viewers)

    override fun setGroup(targets: Audience, group: PrefixesGroup) {
        AudienceResolver.resolveIds(targets).forEach { id -> setGroup(id, group) }
    }

    override fun setGroup(targets: Audience, group: PrefixesGroup, viewers: Audience) {
        val viewerIds = AudienceResolver.resolveIds(viewers)
        AudienceResolver.resolveIds(targets).forEach { id -> set(id, PrefixesOverride.of(group), viewerIds) }
    }

    override fun reset(id: UUID) {
        dataRegistry.reset(id)
        notifyDataChange(id)
    }

    override fun reset(targets: Audience) {
        AudienceResolver.resolveIds(targets).forEach(::reset)
    }

    override fun reset(targets: Audience, viewers: Audience) {
        val viewerIds = AudienceResolver.resolveIds(viewers)
        AudienceResolver.resolveIds(targets).forEach { id ->
            viewerIds.forEach { viewer -> dataRegistry.reset(id, viewer) }
            notifyDataChange(id)
        }
    }

    override fun addGroup(group: PrefixesGroup): CompletableFuture<Boolean> = registry.getCurrentGroupProvider().addGroup(group)

    override fun registerGroupProvider(provider: GroupProvider) = registry.register(provider)

    override fun unregisterGroupProvider(name: String) = registry.unregister(name)

    override fun getGroupProvider(): GroupProvider = registry.getCurrentGroupProvider()

    override fun getGroupProviders(): Collection<GroupProvider> = registry.getAllGroupProviders()

    override fun refreshPlayer(id: UUID) { listeners.forEach { it.onPlayerUpdate(id) } }

    override fun refreshAll() { listeners.forEach(PrefixesListener::onAllPlayersUpdate) }

    private fun set(id: UUID, override: PrefixesOverride) {
        if (!platform.isOnline(id)) return
        dataRegistry.set(id, override)
        notifyDataChange(id)
    }

    private fun set(id: UUID, override: PrefixesOverride, viewers: Audience) {
        set(id, override, AudienceResolver.resolveIds(viewers))
    }

    private fun set(id: UUID, override: PrefixesOverride, viewers: List<UUID>) {
        if (!platform.isOnline(id)) return
        viewers.forEach { viewer -> dataRegistry.set(id, viewer, override) }
        notifyDataChange(id)
    }

    private suspend fun findGroup(name: String): PrefixesGroup? {
        return getGroups().await().firstOrNull { group -> group.name.equals(name, ignoreCase = true) }
    }

    private fun notifyDataChange(id: UUID) {
        listeners.forEach { listener -> listener.onPlayerDataChange(id) }
    }
}
