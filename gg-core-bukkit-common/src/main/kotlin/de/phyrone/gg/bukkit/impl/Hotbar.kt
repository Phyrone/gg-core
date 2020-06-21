package de.phyrone.gg.bukkit.impl

import de.phyrone.gg.bukkit.hotbar.PlayerHotbar
import de.phyrone.gg.bukkit.items.*
import de.phyrone.gg.bukkit.utils.getTargetEntitySafe
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*

private const val HB_ITEM_SLOT_NBT_TAG = "hb.item.slot"

class HotbarPlayerManager(private val plugin: Plugin) : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    private val players = WeakHashMap<Player, HotbarPlayerImpl>()
    fun remove(player: Player) {
        players.remove(player)?.deleted()
    }

    fun getOrCreate(player: Player): PlayerHotbar = players.getOrPut(player) { HotbarPlayerImpl(player, plugin) }


    operator fun get(player: Player): PlayerHotbar? = players[player]

    @EventHandler
    private fun onClick(event: PlayerInteractEvent) {
        val player = players[event.player] ?: return
        val interaction = when (event.action) {
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> Interactive.Interaction.LEFT_CLICK
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> Interactive.Interaction.RIGHT_CLICK
            else -> return
        }
        event.isCancelled = true
        event.player.getTargetEntitySafe()
        player.pushInteraction(player.currentSlot, interaction, event.player.isSneaking)
    }

    @EventHandler
    private fun onDrop(event: PlayerDropItemEvent) {
        val player = players[event.player] ?: return
        val slot = NBTItem(event.itemDrop.itemStack).getInteger(HB_ITEM_SLOT_NBT_TAG) ?: return
        event.isCancelled = true
        player.pushInteraction(slot, Interactive.Interaction.DROP, event.player.isSneaking)
    }

    @EventHandler
    private fun onSelect(event: PlayerItemHeldEvent) {
        val player = players[event.player] ?: return
        player.pushInteraction(event.newSlot, Interactive.Interaction.DROP, event.player.isSneaking)
    }

    @EventHandler
    private fun onSelect(event: PlayerSwapHandItemsEvent) {
        val player = players[event.player] ?: return
        event.isCancelled = true
        player.pushInteraction(player.currentSlot, Interactive.Interaction.OFFHAND_SWAP, event.player.isSneaking)
    }

}

private class HotbarPlayerImpl(private val player: Player, private val plugin: Plugin) : PlayerHotbar {
    init {
        clearSlots()
    }

    private val items = Array<InteractiveItem?>(9) { null }
    private val inv by lazy { player.inventory }

    val currentSlot: Int
        get() = inv.heldItemSlot


    override fun setItem(slot: Int, item: DynamicItem) {

        setItem(slot, item, false)
    }

    override fun setItem(slot: Int, item: DynamicItem, keepInteraction: Boolean) {
        checkSlot(slot)
        val interactive = if (keepInteraction) {
            item.toInteractive()
        } else {
            val interactive = items[slot]
            if (interactive == null) {
                item.toInteractive()
            } else {
                item.withInteractive(interactive)
            }

        }
        setItem(slot, interactive)
    }


    override fun setItem(slot: Int, item: InteractiveItem) {
        setItemLater(slot, item)
    }

    override fun setItem(slot: Int, item: ItemStack) {
        setItem(slot, item, false)
    }

    override fun setItem(slot: Int, item: ItemStack, keepInteraction: Boolean) {
        setItem(slot, item.toDynamic(), keepInteraction)
    }

    private fun checkSlot(slot: Int) {
        if (slot in 0..8) throw IndexOutOfBoundsException("you need to set the item in a range between 0 to 8")
    }

    private fun setItemLater(slot: Int, dynamicItem: InteractiveItem?) {
        checkSlot(slot)
        val preItemStack = dynamicItem?.get(player)
        val itemStack = if (preItemStack == null) null else NBTItem(preItemStack).also {
            it.setInteger(HB_ITEM_SLOT_NBT_TAG, slot)
        }.item
        Bukkit.getScheduler().runTask(plugin, Runnable {
            items[slot] = dynamicItem
            if (itemStack == null) {
                inv.clear(slot)
            } else {

                inv.setItem(slot, itemStack)
            }
        })
    }

    override fun removeItem(slot: Int) {
        setItemLater(slot, null)
    }

    override fun pushInteraction(slot: Int, type: Interactive.Interaction, shift: Boolean) {
        pushInteraction(slot, type, shift, null)
    }

    override fun pushInteraction(slot: Int, type: Interactive.Interaction, shift: Boolean, target: Entity?) {
        val item = items[slot] ?: return
        item.exec(player, target, type, shift)
    }

    private inline fun clearSlots() {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            repeat(9) { slot ->
                inv.clear(slot)
            }
        })
    }

    fun deleted() {
        clearSlots()
    }


}