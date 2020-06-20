package de.phyrone.plugincore.placeholder

import org.bukkit.entity.Player

class PlaceholderManager {

    @JvmOverloads
    fun setPlaceholders(text: String, player: Player? = null): String {
//TODO("implement")
        return text
    }

    @JvmOverloads
    fun setPlaceholders(texts: List<String>, player: Player? = null) =
        texts.map { text -> setPlaceholders(text, player) }
}