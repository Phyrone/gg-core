package de.phyrone.gg.database.provider

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.phyrone.gg.KOIN_PLUGIN_NAME
import de.phyrone.gg.common.config.DatabaseConfigSpec
import de.phyrone.gg.common.config.Konf
import org.koin.core.Koin
import org.koin.core.qualifier.named
import javax.sql.DataSource

abstract class AbstractHikariProvider : DatasourceProvider {

    open val driver: String? = null
    abstract fun getURL(config: Konf, koin: Koin): String
    open fun HikariConfig.override() {}
    override fun get(config: Konf, koin: Koin): DataSource {
        val hikariConfig = HikariConfig()

        hikariConfig.jdbcUrl = config[DatabaseConfigSpec.url].takeUnless { it.isBlank() } ?: getURL(config, koin)
        val username = config[DatabaseConfigSpec.Credentials.userName]
        if (username.isNotBlank()) {
            hikariConfig.username = username
        }
        val password = config[DatabaseConfigSpec.Credentials.password]
        if (password.isNotBlank()) {
            hikariConfig.password = password
        }
        val driver = this.driver
        if (driver != null) {
            hikariConfig.driverClassName = driver
        }
        hikariConfig.minimumIdle = config[DatabaseConfigSpec.Pool.min].coerceAtLeast(0)
        hikariConfig.maximumPoolSize =
            config[DatabaseConfigSpec.Pool.max].coerceAtLeast(1).coerceAtLeast(hikariConfig.minimumIdle)
        hikariConfig.connectionTimeout = config[DatabaseConfigSpec.Pool.connectionTimeout].coerceAtLeast(0)
        hikariConfig.maxLifetime = config[DatabaseConfigSpec.Pool.maximumLifetime].coerceAtLeast(0)
        hikariConfig.poolName = koin.get(named(KOIN_PLUGIN_NAME))
        hikariConfig.override()
        return HikariDataSource(hikariConfig)
    }
}