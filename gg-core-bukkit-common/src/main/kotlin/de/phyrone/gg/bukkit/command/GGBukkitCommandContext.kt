package de.phyrone.gg.bukkit.command

import org.bukkit.command.CommandSender

data class GGBukkitCommandContext(val commandSender: CommandSender, val alias: String, val args: List<String>)