import de.phyrone.gg.module.AbstractMultiThreadedModuleManager
import de.phyrone.gg.module.GGModule
import de.phyrone.gg.module.annotations.ModuleDependencies
import de.phyrone.gg.module.annotations.ModuleName
import org.junit.Test
import java.util.concurrent.Executors

class ModuleLoaderTest {

    @Test
    fun test() {
        val moduleLoader = TestModuleManager()

        println("Enable Modules")
        moduleLoader.onEnable()

        println("Reload Modules")
        moduleLoader.onReload()

        println("Reload Modules Again")
        moduleLoader.onReload()

        println("Disable Modules")
        moduleLoader.onDisable()
    }
}

class TestModuleManager : AbstractMultiThreadedModuleManager(Executors.newFixedThreadPool(4)) {
    override fun getModules() = listOf(
        TestModuleA,
        TestModuleB,
        TestModuleC,
        TestModuleD,
        TestModuleE
    )
}

@ModuleName("A")
object TestModuleA : TestModule("A")

@ModuleName("B")
@ModuleDependencies(["C"])
object TestModuleB : TestModule("B")

@ModuleName("C")
@ModuleDependencies(["A"])
object TestModuleC : TestModule("C")

@ModuleName("D")
object TestModuleD : TestModule("D")

@ModuleName("E")
object TestModuleE : TestModule("E")

open class TestModule(private val name: String) : GGModule {
    override fun onEnable() {
        println("Enable $name ${Thread.currentThread().name}")

//workHard(1000)
    }

    override fun onDisable() {
        println("Disable $name ${Thread.currentThread().name}")
        //      workHard(500)
    }

    override fun onReload() {
        println("Reload $name ${Thread.currentThread().name}")
        //    workHard(1000)
    }

    private fun workHard(time: Long) {
        val finishTime = System.currentTimeMillis() + time
        while (System.currentTimeMillis() < finishTime) {
            //äh yes a realy hard job to do
            1 + 1
        }
    }
}