package de.phyrone.gg.database.provider

import com.zaxxer.hikari.HikariConfig
import de.phyrone.gg.KOIN_FOLDER_DATA
import de.phyrone.gg.KOIN_PLUGIN_NAME
import de.phyrone.gg.common.config.DatabaseConfigSpec
import de.phyrone.gg.common.config.Konf
import org.koin.core.Koin
import org.koin.core.qualifier.named
import java.io.File

object H2FileDatabaseProvider : AbstractHikariProvider() {
    override fun getURL(config: Konf, koin: Koin): String {
        return "jdbc:h2:file:${koin.get<File>(named(KOIN_FOLDER_DATA)).absolutePath}${File.separator}" +
                (config[DatabaseConfigSpec.database].takeUnless { it.isNotBlank() }
                    ?: koin.get(named(KOIN_PLUGIN_NAME)))
    }

    override fun HikariConfig.override() {
        minimumIdle = 1
        maximumPoolSize = 1
        maxLifetime = Long.MAX_VALUE
        connectionTimeout = Long.MAX_VALUE
    }

    override val names: Set<String> = setOf("h2", "local", "file", "h2-file", "h2-local")
}