package de.phyrone.gg

import de.phyrone.gg.bukkit.GGBukkitCorePlugin
import org.bukkit.plugin.java.JavaPlugin

private typealias BungeePlugin = net.md_5.bungee.api.plugin.Plugin
private typealias BukkitPlugin = org.bukkit.plugin.Plugin

object GGCore {
    @JvmStatic
    fun getInstance(plugin: BukkitPlugin) = JavaPlugin.getPlugin(GGBukkitCorePlugin::class.java).getApi(plugin)

    @JvmStatic
    fun getInstance(plugin: BungeePlugin): GGApi {
        TODO()
    }
}