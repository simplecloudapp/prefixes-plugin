package app.simplecloud.prefixes.minestom.display

import net.kyori.adventure.text.Component
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityPose
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.other.InteractionMeta

class MinestomNameTag(private val player: Player) {

    private val entity = Entity(EntityType.INTERACTION)

    init {
        entity.updateViewableRule { viewer -> viewer != player }

        edit { meta ->
            meta.width = WIDTH
            meta.height = HEIGHT
            meta.isInvisible = true
            meta.pose = EntityPose.CROAKING
        }
    }

    fun spawn() {
        val instance = player.instance ?: return

        // Spawned as close to its final position as possible, so the client does not
        // interpolate it into place once it becomes a passenger.
        entity.setInstance(instance, player.position.add(0.0, PASSENGER_OFFSET, 0.0))
            .thenRun { player.addPassenger(entity) }
    }

    fun setName(name: Component) {
        entity.set(DataComponents.CUSTOM_NAME, name)
        edit { meta -> meta.isCustomNameVisible = true }
    }

    fun setSneaking(sneaking: Boolean) {
        edit { meta -> meta.isSneaking = sneaking }
    }

    fun remove() {
        if (entity.isRemoved) return

        player.removePassenger(entity)
        entity.remove()
    }

    private fun edit(editor: (InteractionMeta) -> Unit) {
        entity.editEntityMeta(InteractionMeta::class.java) { meta -> editor(meta) }
    }

    private companion object {
        const val WIDTH = 0.6f
        const val HEIGHT = 0.0f
        const val PASSENGER_OFFSET = 1.8
    }
}
