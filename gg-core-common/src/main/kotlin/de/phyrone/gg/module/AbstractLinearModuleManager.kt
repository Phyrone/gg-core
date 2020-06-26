package de.phyrone.gg.module

import de.phyrone.gg.module.annotations.ModuleDependencies
import de.phyrone.gg.module.annotations.ModuleName
import java.util.*
import kotlin.reflect.full.findAnnotation

abstract class AbstractLinearModuleManager : WrappedModuleManager {

    private val moduleList by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { getModules() }

    private fun GGModule.getName() =
        this::class.findAnnotation<ModuleName>()?.name ?: this::class.simpleName ?: UUID.randomUUID().toString()

    private fun GGModule.getDependencies() =
        this::class.findAnnotation<ModuleDependencies>()?.dependencies?.toList() ?: listOf()

    override fun onEnable() {
        moduleList.forEach { it.onEnable() }
    }

    override fun onReload() {
        moduleList.forEach { it.onReload() }
    }

    override fun onDisable() {
        moduleList.forEach { it.onDisable() }
    }

    private data class ModuleWrapper(val module: GGModule, val name: String, val dependencies: List<String>)
}