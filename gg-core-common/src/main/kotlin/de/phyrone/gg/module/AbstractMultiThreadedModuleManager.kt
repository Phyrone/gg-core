package de.phyrone.gg.module

import com.google.common.collect.Sets
import de.phyrone.gg.module.annotations.ModuleDependencies
import de.phyrone.gg.module.annotations.ModuleName
import org.apache.http.annotation.Experimental
import java.io.Closeable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmName

@Experimental
abstract class AbstractMultiThreadedModuleManager(private var executor: ExecutorService) : Closeable,
    WrappedModuleManager {
    private val moduleActionLock = Object()

    //im too bad to use wait notify
    private val moduleActionDoneLock = Object()
    private val moduleWrappers by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { buildModuleList() }
    private operator fun List<ModuleWrapper>.get(name: String) =
        find { moduleWrapper -> moduleWrapper.name.equals(name, true) }

    private val notifyMap = HashMap<ModuleWrapper, List<ModuleWrapper>>()


    private fun buildModuleList(): List<ModuleWrapper> = getModules().map { module ->
        val name = module::class.findAnnotation<ModuleName>()?.name ?: module::class.simpleName ?: module::class.jvmName
        val dependencies = module::class.findAnnotation<ModuleDependencies>()?.dependencies ?: arrayOf()
        ModuleWrapper(module, name, dependencies)
    }.also { modules ->
        buildNotifyMap(modules)
    }

    private fun buildNotifyMap(modules: List<ModuleWrapper>) {
        modules.forEach { module ->
            notifyMap[module] =
                modules.filter { depModuleWrapper -> depModuleWrapper.dependencies.contains(module.name) }
        }
    }

    private val taskDoneSet = Sets.newConcurrentHashSet<ModuleWrapper>()

    private fun notifyModuleStatusReached(finishedModule: ModuleWrapper, targetStatus: ModuleStatus) {
        if (!allModuleStatusReached(targetStatus)) {
            notifyMap[finishedModule]!!.forEach { notifyModule ->
                getExecutor().execute {
                    notifyModule.handleSetStatus(targetStatus, finishedModule)
                }
            }
        }
        synchronized(moduleActionDoneLock) {
            taskDoneSet.add(finishedModule)
            moduleActionDoneLock.notifyAll()
        }
    }

    var fallbackExecutor: ExecutorService? = null
    private fun getExecutor() =
        executor.takeUnless { executor -> executor.isShutdown || executor.isTerminated } ?: fallbackExecutor
        ?: createFallbackExec()

    protected fun createFallbackExec(): ExecutorService = Executors.newSingleThreadExecutor().also { executorService ->
        this.fallbackExecutor = executorService
    }

    @Volatile
    private var jobDone = true
    private fun pushStatus(targetStatus: ModuleStatus) {
        synchronized(moduleActionLock) {
            assert(jobDone)
            jobDone = false
            moduleWrappers.forEach { module ->
                getExecutor().execute {
                    module.handleSetStatus(targetStatus, null)
                }
            }
            waitTaskDone(targetStatus)
            //set back to enabled (or it wont reload again)
            if (targetStatus == ModuleStatus.RELOAD)
                moduleWrappers.forEach { module -> module.reloadDone() }
            moduleWrappers.forEach { module -> module.resetFinishedModules() }
            taskDoneSet.clear()
            jobDone = true
        }
    }

    private fun waitTaskDone(targetStatus: ModuleStatus) {
        while (!allModuleStatusReached(targetStatus)) {
            synchronized(moduleActionDoneLock) {
                moduleActionDoneLock.wait()
            }
        }
    }

    private fun allModuleStatusReached(targetStatus: ModuleStatus) = taskDoneSet.size == moduleWrappers.size


    override fun onEnable() {
        pushStatus(ModuleStatus.ENABLED)
    }

    override fun onDisable() {
        pushStatus(ModuleStatus.DISABLED)
    }

    override fun onReload() {
        pushStatus(ModuleStatus.RELOAD)
    }

    private inner class ModuleWrapper(
        val module: GGModule,
        val name: String,
        val dependencies: Array<String>
    ) {
        @Volatile
        private var status = ModuleStatus.DISABLED


        fun reloadDone() {
            status = ModuleStatus.ENABLED
        }

        private val finishedDepdenciesSet = Sets.newConcurrentHashSet<ModuleWrapper>()

        fun resetFinishedModules() {
            finishedDepdenciesSet.clear()
        }

        fun depdenciesReachedStatus() = finishedDepdenciesSet.size == dependencies.size

        val lock = Any()
        fun handleSetStatus(targetStatus: ModuleStatus, finishedModule: ModuleWrapper?) {
            synchronized(lock) {


                if (status == targetStatus) {
                    //notifyModuleStatusReached(this, targetStatus)
                } else if (finishedModule != null && finishedModule.status == targetStatus) {
                    finishedDepdenciesSet.add(finishedModule)
                }
                if (depdenciesReachedStatus()) {
                    setStatusAction(targetStatus)
                    status = targetStatus
                    notifyModuleStatusReached(this, targetStatus)
                }
            }
        }

        private fun setStatusAction(targetStatus: ModuleStatus) = when (targetStatus) {
            ModuleStatus.ENABLED -> module.onEnable()
            ModuleStatus.DISABLED -> module.onDisable()
            ModuleStatus.RELOAD -> module.onReload()
        }

    }

    private enum class ModuleStatus {
        DISABLED, ENABLED, RELOAD
    }

    override fun close() {
        fallbackExecutor?.shutdown()
        fallbackExecutor = null
    }
}