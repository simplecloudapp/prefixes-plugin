package app.simplecloud.prefixes.minestom.example

import app.simplecloud.prefixes.minestom.PrefixesMinestom
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.instance.block.Block
import java.nio.file.Path

fun main() {
    val server = MinecraftServer.init()

    val instance = MinecraftServer.getInstanceManager().createInstanceContainer()
    instance.setGenerator { unit ->
        unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK)
    }

    MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        event.spawningInstance = instance
        event.player.respawnPoint = Pos(0.0, 41.0, 0.0)
    }

    PrefixesMinestom.builder(Path.of("prefixes"))
        .commands(true)
        .permissionHandler(::hasPermission)
        .enable()

    server.start("0.0.0.0", 25565)
}

private fun hasPermission(sender: CommandSender, permission: String): Boolean {
    if (sender !is Player) return true
    return permission == "simplecloud.prefix.group.default"
}
