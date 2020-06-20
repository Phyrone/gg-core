package de.phyrone.gg.bukkit.hotbar

import java.util.function.Supplier

interface HotbarPlayer {
    val hotbar: PlayerHotbar
    val isEnabled: Boolean
    fun setIsEnabledHandling(handler: Supplier<Boolean>?)

}