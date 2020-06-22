package de.phyrone.gg.database.provider

import com.zaxxer.hikari.HikariConfig
import de.phyrone.gg.common.config.Konf
import org.koin.core.Koin
import java.util.*

object H2MemDatabaseProvider : AbstractHikariProvider() {
    override fun getURL(config: Konf, koin: Koin): String {
        return "jdbc:h2:mem:${UUID.randomUUID()}"
    }

    override fun HikariConfig.override() {
        minimumIdle = 1
    }

    override val names: Set<String> = setOf("h2-memory", "h2-mem", "mem", "memory")
}