package de.phyrone.gg.bukkit.utils

import fr.mrmicky.fastboard.FastBoard
import org.bukkit.entity.Player
import java.util.*

val Player.fastBoard: FastBoard
    get() = fastBardMap[this]?.takeUnless { it.isDeleted } ?: FastBoard(this)
        .also { fastBoard -> fastBardMap[this] = fastBoard }

private val fastBardMap = WeakHashMap<Player, FastBoard>()