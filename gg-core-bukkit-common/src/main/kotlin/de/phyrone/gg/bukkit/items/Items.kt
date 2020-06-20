package de.phyrone.plugincore.items

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@JvmOverloads
fun DynamicItem.toInteractive(block: (issuer: Player, interaction: Interactive.Interaction, shift: Boolean) -> Unit = { _, _, _ -> }): InteractiveItem =
    if (this is InteractiveItem) this else DelInteractiveItem(this, block)

private class DelInteractiveItem(
    item: DynamicItem,
    val block: (issuer: Player, interaction: Interactive.Interaction, shift: Boolean) -> Unit
) : DynamicItem by item,
    InteractiveItem {

    override fun exec(issuer: Player, target: Entity?, interaction: Interactive.Interaction, shift: Boolean) {
        block(issuer, interaction, shift)
    }
}

fun ItemStack.toDynamic(): DynamicItem =
    StaticDynamicItem(this)

private class StaticDynamicItem(val item: ItemStack) : DynamicItem {
    override fun get(player: Player?) = item

}

fun DynamicItem.withInteractive(interactive: Interactive): InteractiveItem {
    val interactive = if (interactive is IntertactiveDynamicCombindingItem) interactive.interactive else interactive
    return IntertactiveDynamicCombindingItem(this, interactive)
}

private class IntertactiveDynamicCombindingItem(val dynamicItem: DynamicItem, val interactive: Interactive) :
    InteractiveItem,
    DynamicItem by dynamicItem, Interactive by interactive