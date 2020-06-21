package de.phyrone.gg.bukkit.hotbar

import de.phyrone.gg.bukkit.GGBukkitCorePlugin
import org.bukkit.entity.Player


val Player.hotbar: PlayerHotbar?
    get() = GGBukkitCorePlugin.getInstance().hotbarManager[this]

fun Player.removeHotbar() {
    GGBukkitCorePlugin.getInstance().hotbarManager.remove(this)
}

fun Player.getOrCreateHotbar() = GGBukkitCorePlugin.getInstance().hotbarManager.getOrCreate(this)
