package de.phyrone.gg.module

interface ModuleManager {
    fun registerModules(modules: List<Class<out GGModule>>)
    fun onEnable()
    fun onReload()
    fun onDisable()
}