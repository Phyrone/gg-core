package de.phyrone.gg.database.provider

import com.zaxxer.hikari.HikariConfig
import de.phyrone.gg.KOIN_PLUGIN_NAME
import de.phyrone.gg.common.config.Konf
import org.koin.core.Koin
import org.koin.core.qualifier.named

object H2MemDatabaseProvider : AbstractHikariProvider() {
    override val driver: String = "org.h2.Driver"
    override fun getURL(config: Konf, koin: Koin): String {
        return "jdbc:h2:mem:${koin.get<String>(named(KOIN_PLUGIN_NAME))};DB_CLOSE_DELAY=-1"
    }

    override fun HikariConfig.override() {
        minimumIdle = 0
        maximumPoolSize = Int.MAX_VALUE
        idleTimeout = 1000
        maxLifetime = Long.MAX_VALUE

    }

    override val names: Set<String> = setOf("h2-memory", "h2-mem", "mem", "memory")
}