package de.phyrone.gg.bukkit.threads

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import java.util.*
import java.util.concurrent.Executor

private class SchedulerExecutor(private val plugin: Plugin, private val async: Boolean) : Executor {
    private val scheduler by lazy { Bukkit.getScheduler() }
    override fun execute(command: Runnable) {
        if (async) scheduler.runTaskAsynchronously(plugin, command)
        else scheduler.runTask(plugin, command)
    }
}

private val syncSchedulerCoroutineCache = WeakHashMap<Plugin, SchedulerExecutor>()
private val asyncSchedulerCoroutineCache = WeakHashMap<Plugin, SchedulerExecutor>()

@JvmOverloads
fun BukkitScheduler.coroutineScope(plugin: Plugin, async: Boolean = true): CoroutineDispatcher =
    (if (async) asyncSchedulerCoroutineCache else syncSchedulerCoroutineCache)
        .getOrPut(plugin) { SchedulerExecutor(plugin, async) }.asCoroutineDispatcher()
