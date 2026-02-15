package app.simplecloud.plugin.prefixes.api.impl

import app.simplecloud.plugin.prefixes.api.PrefixesActor
import app.simplecloud.plugin.prefixes.api.PrefixesApi
import app.simplecloud.plugin.prefixes.api.PrefixesConfig
import app.simplecloud.plugin.prefixes.api.PrefixesGroup
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.util.*

abstract class PrefixesApiImpl : PrefixesApi {

    private val groups: MutableList<PrefixesGroup> = mutableListOf()
    private var actor: PrefixesActor = PrefixesActorBlankImpl()
    private lateinit var config: PrefixesConfig

    override fun registerViewer(uniqueId: UUID) {
        actor.registerViewer(uniqueId, this)
    }

    override fun hasViewer(uniqueId: UUID): Boolean {
        return actor.hasViewer(uniqueId)
    }

    override fun removeViewer(uniqueId: UUID) {
        actor.removeViewer(uniqueId)
    }

    override fun getGroups(): MutableList<PrefixesGroup> {
        return groups
    }

    override fun getHighestGroup(uniqueId: UUID): PrefixesGroup {
        return groups.stream().filter { group ->
            group.containsPlayer(uniqueId)
        }.findFirst().orElse(null)
    }

    override fun addGroup(group: PrefixesGroup) {
        groups.add(group)
    }

    override fun setActor(actor: PrefixesActor) {
        this.actor = actor
    }

    override fun setWholeName(uniqueId: UUID, group: PrefixesGroup, viewers: Audience) {
        val uuids = toUUIDList(viewers)
        setWholeName(uniqueId, group, *uuids.toTypedArray())
    }

    override fun setWholeName(uniqueId: UUID, groupName: String, viewers: Audience) {
        val group = groups.stream()
            .filter { group -> group.getName() == groupName }
            .findFirst()
            .orElse(null) ?: return

        setWholeName(uniqueId, group, viewers)
    }

    override fun setWholeName(
        uniqueId: UUID,
        prefix: Component,
        color: TextColor,
        suffix: Component,
        priority: Int,
        viewers: Audience
    ) {
        val uuids = toUUIDList(viewers)
        setWholeName(uniqueId, prefix, color, suffix, priority, *uuids.toTypedArray())
    }

    override fun formatChatMessage(target: UUID, viewer: Audience, format: String, message: Component): Component {
        val uuid = toUUID(viewer)
        return actor.formatMessage(target, uuid, format, message)
    }

    override fun setPrefix(uniqueId: UUID, prefix: Component, viewers: Audience) {
        val uuids = toUUIDList(viewers)
        setPrefix(uniqueId, prefix, *uuids.toTypedArray())
    }

    override fun setSuffix(uniqueId: UUID, suffix: Component, viewers: Audience) {
        val uuids = toUUIDList(viewers)
        setSuffix(uniqueId, suffix, *uuids.toTypedArray())
    }

    override fun setColor(uniqueId: UUID, color: TextColor, viewers: Audience) {
        val uuids = toUUIDList(viewers)
        setColor(uniqueId, color, *uuids.toTypedArray())
    }

    override fun setWholeName(uniqueId: UUID, group: PrefixesGroup, vararg viewers: UUID) {
        actor.applyGroup(uniqueId, group, *viewers)
    }

    override fun setWholeName(uniqueId: UUID, groupName: String, vararg viewers: UUID) {
        val group = groups.stream()
            .filter { group -> group.getName() == groupName }
            .findFirst()
            .orElse(null) ?: return

        setWholeName(uniqueId, group, *viewers)
    }

    override fun setWholeName(
        uniqueId: UUID,
        prefix: Component,
        color: TextColor,
        suffix: Component,
        priority: Int,
        vararg viewers: UUID
    ) {
        actor.apply(uniqueId, prefix, color, suffix, priority, *viewers)
    }

    override fun setPrefix(uniqueId: UUID, prefix: Component, vararg viewers: UUID) {
        actor.setPrefix(uniqueId, prefix, *viewers)
    }

    override fun setSuffix(uniqueId: UUID, suffix: Component, vararg viewers: UUID) {
        actor.setSuffix(uniqueId, suffix, *viewers)
    }

    override fun setColor(uniqueId: UUID, color: TextColor, vararg viewers: UUID) {
        actor.setColor(uniqueId, color, *viewers)
    }

    override fun setConfig(config: PrefixesConfig) {
        this.config = config
    }

    fun getConfig(): PrefixesConfig {
        return config
    }

    override fun formatChatMessage(target: UUID, viewer: UUID?, format: String, message: Component): Component {
        return actor.formatMessage(target, viewer, format, message)
    }

    private fun toUUIDList(audience: Audience): List<UUID> {
        val uuids = mutableListOf<UUID>()
        audience.forEachAudience forEachPlayer@ {
            val uuid = it.get(Identity.UUID).orElse(null) ?: return@forEachPlayer
            uuids.add(uuid)
        }
        return uuids
    }

    private fun toUUID(audience: Audience): UUID? {
        return audience.get(Identity.UUID).orElse(null)
    }

    abstract fun indexGroups()

}