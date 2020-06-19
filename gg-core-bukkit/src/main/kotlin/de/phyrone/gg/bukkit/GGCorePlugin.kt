package de.phyrone.gg.bukkit

import de.phyrone.gg.GGApi
import de.phyrone.gg.module.AbstractModuleManager
import de.phyrone.gg.module.GGApiProvider
import de.phyrone.gg.module.GGModule
import de.phyrone.gg.module.ModuleManager
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Supplier

private typealias PluginMarker = kr.entree.spigradle.Plugin

@PluginMarker
class GGCorePlugin : JavaPlugin(), GGApiProvider<Plugin, GGBukkitApi> {

    private val apiMap = WeakHashMap<Plugin, GGCoreApiImpl>()
    lateinit var executorService: ExecutorService
    override fun getApi(target: Plugin): GGBukkitApi = apiMap[target] ?: GGCoreApiImpl(target, this).also { api ->
        apiMap[target] = api
    }

    override fun onEnable() {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors().coerceAtLeast(2))
    }

    override fun onDisable() {
        executorService.shutdown()
    }
}

private class GGCoreApiImpl(
    private val targetPlugin: Plugin,
    private val corePlugin: GGCorePlugin
) : GGBukkitApi {

    private val koinApp by lazy {
        startKoin {
            modules(module {
                single<KoinApplication>(override = true) { this@startKoin }
                single<Koin>(override = true) { this@startKoin.koin }
                single<GGBukkitApi> { this@GGCoreApiImpl }
                single<GGApi> { this@GGCoreApiImpl }
                single { targetPlugin }
                single { Bukkit.getScheduler() }
                single { Bukkit.getConsoleSender() }
                single { Bukkit.getScoreboardManager() }
            })
        }
    }
    private val koin by lazy { koinApp.koin }
    override val moduleManager: ModuleManager by lazy { GGCoreModuleManagerImpl(koin, corePlugin.executorService) }

    private class GGCoreModuleManagerImpl(
        private val koin: Koin,
        executorService: ExecutorService
    ) : ModuleManager, AbstractModuleManager(executorService) {

        lateinit var getModulesHandler: Supplier<List<Class<out GGModule>>>
        override fun getModuleHandler(getModulesHandler: Supplier<List<Class<out GGModule>>>) {
            this.getModulesHandler = getModulesHandler
        }

        override fun onEnable() {
            enableModules()
        }

        override fun onReload() {
            reloadModules()
        }

        override fun onDisable() {
            disableModules()
        }


        override fun getModules(): List<GGModule> = getModulesHandler.get()
            .map { moduleClass -> moduleClass.getConstructor(Koin::class.java).newInstance(koin) }

    }
}