package de.phyrone.gg.module

import java.util.function.Consumer
import java.util.function.Supplier

interface ModuleManager {
    fun getModuleHandler(getModulesHandler: Supplier<List<Class<GGModule>>>)
    fun onEnable()
    fun onReload()
    fun onDisable()
}