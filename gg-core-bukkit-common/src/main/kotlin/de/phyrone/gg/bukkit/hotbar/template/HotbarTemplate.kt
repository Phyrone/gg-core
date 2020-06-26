package de.phyrone.gg.bukkit.hotbar.template

import de.phyrone.gg.bukkit.hotbar.PlayerHotbar
import org.bukkit.entity.Player

interface HotbarTemplate {
    fun PlayerHotbar.append(player: Player)

    /**
     * @return true to reload the template false to do nothing
     */
    fun PlayerHotbar.next(player: Player): Boolean

    /**
     * @return true to reload the template false to do nothing
     */
    fun PlayerHotbar.prev(player: Player): Boolean

    fun copy(): HotbarTemplate
}