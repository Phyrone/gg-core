package de.phyrone.gg.database

import de.phyrone.gg.KOIN_CONFIG_MAIN
import de.phyrone.gg.common.config.DatabaseConfigSpec
import de.phyrone.gg.common.config.Konf
import de.phyrone.gg.database.provider.DatasourceProvider
import de.phyrone.gg.database.provider.H2FileDatabaseProvider
import de.phyrone.gg.database.provider.H2MemDatabaseProvider
import de.phyrone.gg.database.provider.MySQLDatabaseProvider
import de.phyrone.gg.module.DefaultGGModule
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.Closeable
import java.util.logging.Logger
import javax.sql.DataSource

open class DatabaseModule(koin: Koin) : DefaultGGModule(koin) {
    private val config by inject<Konf>(named(KOIN_CONFIG_MAIN))
    private val koinApplication by inject<KoinApplication>()
    private val logger by inject<Logger>()
    private val fallbackProvider = H2FileDatabaseProvider

    private var datasource: DataSource? = null
    final override fun onEnable() {
        initDatabase()
    }

    private fun initDatabase() {
        val datasource = loadDataSource()
        this.datasource = datasource
        val database = Database.connect(datasource)
        transaction(database) {
            onDatabaseStart()
        }
        koinApplication.modules(module {
            single(createdAtStart = true, override = true) { datasource }
            single(createdAtStart = true, override = true) { database }
        })
    }

    open fun Transaction.onDatabaseStart() {}

    @Suppress("NOTHING_TO_INLINE")
    private inline fun loadDataSource(): DataSource {
        val providerName = config[DatabaseConfigSpec.provider].toLowerCase()
        val provider = providers.getOrElse(providerName) {
            logger.warning("Database provider " + providerName + "not fround -> fall back to h2")
            fallbackProvider
        }
        return provider[config, getKoin()]
    }

    final override fun onDisable() {
        closeDataSource()
    }

    final override fun onReload() {
        closeDataSource()
        initDatabase()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun closeDataSource() {
        (datasource as? Closeable)?.close()
        datasource = null
    }

    companion object Static {
        private val providers = HashMap<String, DatasourceProvider>()

        init {
            listOf(H2FileDatabaseProvider, H2MemDatabaseProvider, MySQLDatabaseProvider).forEach { provider ->
                provider.names.forEach { name ->
                    providers[name.toLowerCase().trim()] = provider
                }
            }
        }
    }

}