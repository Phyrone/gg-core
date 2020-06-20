package de.phyrone.gg.bukkit.utils

import fr.mrmicky.fastboard.FastBoard
import org.bukkit.entity.Player
import java.util.*

val Player.fastBoard: FastBoard
    get() = fastBardMap.getOrPut(this) { FastBoard(this) }

private val fastBardMap = WeakHashMap<Player, FastBoard>()