package app.simplecloud.plugin.prefixes.minestom

import app.simplecloud.plugin.prefixes.api.PrefixesApi
import app.simplecloud.plugin.prefixes.api.PrefixesGroup
import app.simplecloud.plugin.prefixes.api.impl.PrefixesApiImpl
import app.simplecloud.plugin.prefixes.api.impl.PrefixesConfigImpl
import app.simplecloud.plugin.prefixes.shared.MiniMessageImpl
import app.simplecloud.plugin.prefixes.shared.PrefixesApiLuckPermsImpl
import app.simplecloud.plugin.prefixes.shared.PrefixesConfigParser
import jdk.jfr.Experimental
import me.lucko.luckperms.minestom.LPMinestomPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.extensions.Extension
import java.io.File

class PrefixesExtension : Extension() {

    companion object {
        private lateinit var prefixesApi: PrefixesApiImpl
        fun getApi(): PrefixesApi {
            return prefixesApi
        }
    }

    override fun initialize() {

        @Experimental
        prefixesApi =
            PrefixesApiLuckPermsImpl(LPMinestomPlugin.getApi()) //! NO OFFICIAL LUCKPERMS SUPPORT RELEASED YET !
        prefixesApi.setActor(PrefixesActorMinestomImpl(PrefixesGlobalDisplayMinestomImpl()))
        val config = PrefixesConfigParser<PrefixesConfigImpl>(File(dataDirectory.toFile(), "config.json")).parse(
            PrefixesConfigImpl::class.java,
            PrefixesConfigImpl()
        )
        savePackagedResource("config.json")
        prefixesApi.setConfig(config)
        prefixesApi.indexGroups()
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
            prefixesApi.registerViewer(event.player.uuid)
            val prefixesGroup: PrefixesGroup = prefixesApi.getHighestGroup(event.player.uuid)
            prefixesApi.setWholeName(event.player.uuid, prefixesGroup)
        }
        MinecraftServer.getGlobalEventHandler().addListener(PlayerChatEvent::class.java) { event ->
            event.setChatFormat {
                return@setChatFormat prefixesApi.formatChatMessage(
                    event.player.uuid,
                    event.player.uuid,
                    prefixesApi.getConfig().getChatFormat(),
                    MiniMessageImpl.parse(event.message)
                )
            }
        }

        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent::class.java) { event ->
            prefixesApi.removeViewer(event.player.uuid)
        }
        MinecraftServer.LOGGER.info(Component.text("PrefixesApi initialized.").color(TextColor.color(0x7cf7ab)))
    }

    override fun terminate() {

    }


}