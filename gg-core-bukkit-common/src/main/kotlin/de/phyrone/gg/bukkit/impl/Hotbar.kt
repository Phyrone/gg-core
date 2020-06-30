package de.phyrone.gg.bukkit.impl

import de.phyrone.gg.bukkit.hotbar.PlayerHotbar
import de.phyrone.gg.bukkit.hotbar.template.HotbarTemplate
import de.phyrone.gg.bukkit.items.*
import de.phyrone.gg.bukkit.utils.getTargetEntitySafe
import de.tr7zw.changeme.nbtapi.NBTItem
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
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

    @EventHandler(ignoreCancelled = true)
    private fun onInvClick(event: InventoryClickEvent) {
        val inv = event.inventory
        val player = (event.whoClicked as? Player) ?: return
        players[player] ?: return
        when (inv.type) {
            InventoryType.PLAYER, InventoryType.CREATIVE, InventoryType.CRAFTING -> {
                if (event.slot in (0..8)) {
                    event.isCancelled = true
                    if (player.gameMode == GameMode.CREATIVE)
                        event.view.cursor = null
                }
            }
            else -> return
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPickup(event: EntityPickupItemEvent) {
        val entity = event.entity
        if (entity is Player) {
            if (players.containsKey(entity))
                event.isCancelled = true
        }
    }

    @EventHandler
    private fun onClick(event: PlayerInteractEvent) {
        val player = players[event.player] ?: return
        val interaction = when (event.action) {
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> Interactive.Interaction.LEFT_CLICK
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> Interactive.Interaction.RIGHT_CLICK
            else -> return
        }
        event.isCancelled = true
        val slot = NBTItem(event.item ?: return).getInteger(HB_ITEM_SLOT_NBT_TAG) ?: return
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            player.pushInteraction(
                slot,
                interaction,
                event.player.isSneaking,
                event.player.getTargetEntitySafe()
            )
        })

    }

    @EventHandler
    private fun onDrop(event: PlayerDropItemEvent) {
        val player = players[event.player] ?: return
        event.isCancelled = true
        val slot = NBTItem(event.itemDrop.itemStack).getInteger(HB_ITEM_SLOT_NBT_TAG) ?: return
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            player.pushInteraction(
                slot,
                Interactive.Interaction.DROP,
                event.player.isSneaking,
                event.player.getTargetEntitySafe()
            )
        })

    }

    @EventHandler
    private fun onSelect(event: PlayerItemHeldEvent) {
        val player = players[event.player] ?: return
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            player.pushInteraction(
                event.newSlot,
                Interactive.Interaction.HOVER,
                event.player.isSneaking,
                event.player.getTargetEntitySafe()
            )
        })
        when ((event.previousSlot - event.newSlot)) {
            7, 8 -> player.next()
            -7, -8 -> player.prev()
        }
    }

    @EventHandler
    private fun onSwap(event: PlayerSwapHandItemsEvent) {
        val player = players[event.player] ?: return
        event.isCancelled = true
        val slot = NBTItem(event.offHandItem?.takeUnless { it.type == Material.AIR } ?: return).getInteger(
            HB_ITEM_SLOT_NBT_TAG
        ) ?: return
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            player.pushInteraction(
                slot,
                Interactive.Interaction.OFFHAND_SWAP,
                event.player.isSneaking,
                event.player.getTargetEntitySafe()
            )
        })
    }

}

private class HotbarPlayerImpl(private val player: Player, private val plugin: Plugin) : PlayerHotbar {
    init {
        clearSlots()
    }

    private val items = Array<InteractiveItem?>(9) { null }
    private val inv by lazy { player.inventory }


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

        if (slot !in 0..8) throw IndexOutOfBoundsException("you need to set the item in a range between 0 to 8")
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


    override fun pushInteraction(slot: Int, type: Interactive.Interaction, shift: Boolean, target: Entity?) {
        val item = items[slot] ?: return
        item.exec(player, target, type, shift)
    }

    override fun clear() {
        clear(true)
    }

    override fun clear(keepInteraction: Boolean) {
        if (keepInteraction) clearSlots() else clearItems()
    }


    @Suppress("NOTHING_TO_INLINE")
    private inline fun clearSlots() {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            repeat(9) { slot ->
                inv.clear(slot)
            }
        })
    }

    private fun clearItems() {
        repeat(9) { slot ->
            items[slot] = null
        }
        clearSlots()
    }

    override fun next() {
        handleTemplateIfExist { next(player).reloadIfNeeded() }
    }

    override fun prev() {
        handleTemplateIfExist { prev(player).reloadIfNeeded() }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Boolean.reloadIfNeeded() {
        if (this) reloadTemplate()
    }

    private inline fun <T> handleTemplateIfExist(block: HotbarTemplate.() -> T): T? {
        val template = template
        return if (template != null) block.invoke(template) else null
    }

    override fun reloadTemplate() {
        clearItems()
        handleTemplateIfExist {
            append(player)
        }
    }

    override var template: HotbarTemplate? = null
        set(value) {
            field = value
            reloadTemplate()
        }

    fun deleted() {
        clearSlots()
    }


}