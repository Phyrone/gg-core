package de.phyrone.plugincore.command

import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.CommandNode
import de.phyrone.gg.bukkit.command.GGBukkitCommandContext
import org.bukkit.command.CommandSender

interface HelpCommandHandler {
    fun noCommand(
        sender: CommandSender,
        label: String,
        node: CommandNode<GGBukkitCommandContext>,
        parsed: ParseResults<out GGBukkitCommandContext>
    ): Boolean

    fun unknownCommand(
        sender: CommandSender,
        label: String,
        args: Array<out String>,
        parsed: ParseResults<GGBukkitCommandContext>,
        lastCorrectCMD: String,
        lastCorrectNode: CommandNode<GGBukkitCommandContext>,
        exception: CommandSyntaxException
    ): Boolean

    fun wrongCommandResultHandler(
        sender: CommandSender,
        label: String,
        args: Array<out String>,
        node: CommandNode<GGBukkitCommandContext>,
        parsed: ParseResults<GGBukkitCommandContext>, result: Int
    ): Boolean
}