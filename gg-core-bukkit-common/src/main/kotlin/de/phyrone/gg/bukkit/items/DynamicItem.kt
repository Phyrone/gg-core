package de.phyrone.gg.bukkit.items

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface DynamicItem {
    operator fun get(player: Player? = null): ItemStack
}