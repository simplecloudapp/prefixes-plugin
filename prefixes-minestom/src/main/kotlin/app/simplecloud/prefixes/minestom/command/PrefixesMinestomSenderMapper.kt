package app.simplecloud.prefixes.minestom.command

import app.simplecloud.prefixes.minestom.permission.MinestomPermissions
import net.minestom.server.command.CommandSender
import org.incendo.cloud.SenderMapper

class PrefixesMinestomSenderMapper(
    private val permissions: MinestomPermissions
) : SenderMapper<CommandSender, PrefixesMinestomSender> {

    override fun map(base: CommandSender): PrefixesMinestomSender = PrefixesMinestomSender(base, permissions)

    override fun reverse(mapped: PrefixesMinestomSender): CommandSender = mapped.sender

}
