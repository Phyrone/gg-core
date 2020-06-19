package de.phyrone.gg.module

import org.koin.core.Koin
import org.koin.core.KoinComponent

interface GGModule : KoinComponent {

    fun onEnable() {}

    fun onDisable() {}

    fun onReload() {}

}

open class BasicGGModule(
    private val koin: Koin
) : GGModule {
    override fun getKoin() = koin

    override fun onEnable() {}

    override fun onDisable() {}

    override fun onReload() {}
}