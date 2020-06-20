package de.phyrone.gg.bukkit.items

import org.bukkit.entity.Entity
import org.bukkit.entity.Player

interface Interactive {
    fun exec(issuer: Player, target: Entity?, interaction: Interaction, shift: Boolean)
    enum class Interaction {
        LEFT_CLICK, RIGHT_CLICK, DROP, OFFHAND_SWAP, HOVER
    }
}