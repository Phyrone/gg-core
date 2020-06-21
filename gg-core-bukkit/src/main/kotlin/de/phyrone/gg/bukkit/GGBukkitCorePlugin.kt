package de.phyrone.gg.bukkit

import de.phyrone.brig.wrapper.literal
import de.phyrone.brig.wrapper.runs
import de.phyrone.gg.GGApi
import de.phyrone.gg.KOIN_DATA_FOLDER
import de.phyrone.gg.bukkit.command.BrigadierBukkiCommand
import de.phyrone.gg.bukkit.command.GGBukkitCommandDispatcher
import de.phyrone.gg.bukkit.impl.HotbarPlayerManager
import de.phyrone.gg.bukkit.utils.registerCommand
import de.phyrone.gg.bukkit.utils.sendMessage
import de.phyrone.gg.module.AbstractModuleManager
import de.phyrone.gg.module.GGApiProvider
import de.phyrone.gg.module.GGModule
import de.phyrone.gg.module.ModuleManager
import mkremins.fanciful.FancyMessage
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Supplier

private typealias PluginMarker = kr.entree.spigradle.Plugin

@PluginMarker
class GGBukkitCorePlugin : JavaPlugin(), GGApiProvider<Plugin, GGBukkitApi> {

    private val apiMap = WeakHashMap<Plugin, GGCoreApiImpl>()
    internal var executorService: ExecutorService? = null
    override fun getApi(target: Plugin): GGBukkitApi = apiMap[target] ?: GGCoreApiImpl(target, this).also { api ->
        apiMap[target] = api
    }

    val hotbarManager by lazy { HotbarPlayerManager(this) }

    private val ggCommandDispatcher = GGBukkitCommandDispatcher()
    override fun onEnable() {
        executorService?.shutdown()
        executorService = Executors.newCachedThreadPool()
        regCommand()
    }

    private fun regCommand() {
        ggCommandDispatcher.literal("debug") {
            require { commandSender.hasPermission("gg.debug") }
        }
        ggCommandDispatcher.literal("version") {
            require { commandSender.hasPermission("gg.version") }
            runs {
                commandSender.sendMessage(
                    FancyMessage("GG-CORE").color(ChatColor.AQUA)
                        .then(": ").color(ChatColor.GRAY)
                        .then(description.version).color(ChatColor.GOLD)
                )
            }
        }
        registerCommand(
            BrigadierBukkiCommand(
                "gg",
                ggCommandDispatcher,
                "all commands of the gg-core",
                aliases = listOf("gg-core")
            )
        )
    }

    override fun onDisable() {
        executorService?.shutdown()
        executorService = null
    }

    companion object Static {
        @JvmStatic
        fun getInstance() = getPlugin(GGBukkitCorePlugin::class.java)

    }
}

private class GGCoreApiImpl(
    private val targetPlugin: Plugin,
    private val corePlugin: GGBukkitCorePlugin
) : GGBukkitApi {

    private val koinApp by lazy {
        startKoin {
            modules(module {
                single<KoinApplication>(override = true) { this@startKoin }
                single<Koin>(override = true) { this@startKoin.koin }
                single<GGBukkitApi> { this@GGCoreApiImpl }
                single<GGApi> { this@GGCoreApiImpl }
                single<Plugin> { targetPlugin }
                single { Bukkit.getScheduler() }
                single { Bukkit.getConsoleSender() }
                single { Bukkit.getScoreboardManager() }
                single { targetPlugin.logger }
                single(named(KOIN_DATA_FOLDER)) { targetPlugin.dataFolder }
            })
        }
    }
    private val koin by lazy { koinApp.koin }
    override val moduleManager: ModuleManager by lazy {
        GGCoreModuleManagerImpl(
            corePlugin.executorService ?: error("could not load thread pool for start! is the core enabled?")
        )
    }

    private inner class GGCoreModuleManagerImpl(
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
            .map { moduleClass ->
                moduleClass.getConstructor(Koin::class.java).newInstance(koin).also { module ->
                    if (module is Listener) {
                        Bukkit.getPluginManager().registerEvents(module, targetPlugin)
                    }
                }
            }

    }
}