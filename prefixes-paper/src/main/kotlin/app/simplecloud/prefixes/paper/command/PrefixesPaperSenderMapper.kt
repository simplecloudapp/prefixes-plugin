package app.simplecloud.prefixes.paper.command

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.SenderMapper

class PrefixesPaperSenderMapper : SenderMapper<CommandSourceStack, PrefixesPaperSender> {

    override fun map(base: CommandSourceStack): PrefixesPaperSender = PrefixesPaperSender(base)

    override fun reverse(mapped: PrefixesPaperSender): CommandSourceStack = mapped.stack

}