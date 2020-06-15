package de.phyrone.gg.bukkit

import de.phyrone.gg.GGApi
import de.phyrone.gg.module.GGApiProvider
import de.phyrone.gg.module.GGModule
import de.phyrone.gg.module.ModuleManager
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.KoinContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.*
import kotlin.collections.ArrayList

private typealias PluginMarker = kr.entree.spigradle.Plugin

@PluginMarker
class GGCorePlugin : JavaPlugin(), GGApiProvider<Plugin, GGBukkitApi> {

    private val apiMap = WeakHashMap<Plugin, GGCoreApiImpl>()
    override fun getApi(target: Plugin): GGBukkitApi = apiMap[target] ?: GGCoreApiImpl(target, this).also { api ->
        apiMap[target] = api
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
    override val moduleManager: ModuleManager by lazy { GGCoreModuleManagerImpl(koin) }

    private class GGCoreModuleManagerImpl(
        private val koin: Koin
    ) : ModuleManager {

        private val modules = ArrayList<GGModule>()
        override fun registerModules(modules: List<Class<out GGModule>>) {
            this.modules.addAll(modules.mapNotNull { moduleClass ->
                runCatching { moduleClass.getConstructor(Koin::class.java).newInstance(koin) }.getOrNull()
            })
        }

        override fun onEnable() {
            modules.forEach { module -> module.onEnable() }
        }

        override fun onReload() {
            modules.forEach { module -> module.onReload() }
        }

        override fun onDisable() {
            modules.forEach { module -> module.onDisable() }
        }
    }
}