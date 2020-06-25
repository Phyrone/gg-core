package de.phyrone.gg.module

import java.util.function.Supplier

interface ModuleManager {

    fun onEnable()
    fun onReload()
    fun onDisable()
}

interface WrappedModuleManager : ModuleManager {
    fun getModules(): List<GGModule>
}

interface ModuleManagerApi : ModuleManager {
    fun getModuleHandler(getModulesHandler: Supplier<List<Class<out GGModule>>>)
}