package de.phyrone.gg.bukkit.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.CommandNode
import de.phyrone.plugincore.command.HelpCommandHandler
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand


class BrigadierBukkiCommand @JvmOverloads constructor(
    name: String,
    private val commandDispatcher: CommandDispatcher<GGBukkitCommandContext>,
    description: String = "",
    usage: String = "/$name [args]",
    aliases: List<String> = listOf(),
    private val helpCommandHandler: HelpCommandHandler = DefaultHelpCommandHandler
) : BukkitCommand(name, description, usage, aliases) {

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        val parsed = parse(sender, commandLabel, args)
        val node = parsed.context.nodes.lastOrNull()?.node ?: commandDispatcher.root
        return if (args.isEmpty()) {
            helpCommandHandler.noCommand(sender, label, node, parsed)
        } else {
            try {
                val result = commandDispatcher.execute(parsed)
                return if (result == 0) true else helpCommandHandler.wrongCommandResultHandler(
                    sender,
                    label,
                    args,
                    node,
                    parsed,
                    result
                )
            } catch (exception: CommandSyntaxException) {
                val lastCorrectCmd = exception.input.substring(0, exception.cursor).trim() + " "
                helpCommandHandler.unknownCommand(
                    sender,
                    commandLabel,
                    args,
                    parsed,
                    lastCorrectCmd,
                    node,
                    exception
                )

            }
        }
    }


    private fun parse(sender: CommandSender, alias: String, args: Array<out String>) =
        commandDispatcher.parse(args.joinToString(" "), getContext(sender, alias, args))

    private fun getContext(sender: CommandSender, alias: String, args: Array<out String>) =
        GGBukkitCommandContext(sender, alias, args.toList())

    override fun tabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<out String>,
        location: Location?
    ): List<String> {
        val parsed = parse(sender, alias, args)
        val suggestions = commandDispatcher.getCompletionSuggestions(parsed).get()
        return if (suggestions.isEmpty)
            super.tabComplete(sender, alias, args, location)
        else suggestions.list.map { suggestion -> suggestion.text }
    }

    object DefaultHelpCommandHandler : HelpCommandHandler {
        override fun noCommand(
            sender: CommandSender,
            label: String,
            node: CommandNode<GGBukkitCommandContext>,
            parsed: ParseResults<out GGBukkitCommandContext>
        ): Boolean = true

        override fun unknownCommand(
            sender: CommandSender,
            label: String,
            args: Array<out String>,
            parsed: ParseResults<GGBukkitCommandContext>,
            lastCorrectCMD: String,
            lastCorrectNode: CommandNode<GGBukkitCommandContext>,
            exception: CommandSyntaxException
        ): Boolean {
            sender.sendMessage(exception.localizedMessage)
            return true
        }

        override fun wrongCommandResultHandler(
            sender: CommandSender,
            label: String,
            args: Array<out String>,
            node: CommandNode<GGBukkitCommandContext>,
            parsed: ParseResults<GGBukkitCommandContext>,
            result: Int
        ) = true


    }
}