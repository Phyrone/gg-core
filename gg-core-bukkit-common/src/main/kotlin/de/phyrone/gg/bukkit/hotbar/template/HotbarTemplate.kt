package de.phyrone.gg.bukkit.hotbar.template

import de.phyrone.gg.bukkit.hotbar.PlayerHotbar

interface HotbarTemplate : Cloneable {
    fun PlayerHotbar.append()
}