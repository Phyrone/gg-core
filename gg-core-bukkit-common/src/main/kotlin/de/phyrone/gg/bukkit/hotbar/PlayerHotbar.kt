package de.phyrone.gg.bukkit.hotbar

import de.phyrone.gg.bukkit.hotbar.template.HotbarTemplate
import de.phyrone.gg.bukkit.items.DynamicItem
import de.phyrone.gg.bukkit.items.Interactive
import de.phyrone.gg.bukkit.items.InteractiveItem
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

interface PlayerHotbar {

    fun setItem(slot: Int, item: InteractiveItem)

    fun setItem(slot: Int, item: DynamicItem)
    fun setItem(slot: Int, item: DynamicItem, keepInteraction: Boolean)

    fun setItem(slot: Int, item: ItemStack)
    fun setItem(slot: Int, item: ItemStack, keepInteraction: Boolean)

    fun removeItem(slot: Int)

    fun pushInteraction(slot: Int, type: Interactive.Interaction, shift: Boolean, target: Entity?)

    fun clear()
    fun clear(keepInteraction: Boolean)

    fun next()
    fun prev()
    fun reloadTemplate()
    var template: HotbarTemplate?
}