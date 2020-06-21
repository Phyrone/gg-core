package de.phyrone.gg.database.provider

import de.phyrone.gg.KOIN_FOLDER_DATA
import de.phyrone.gg.common.config.DatabaseConfigSpec
import de.phyrone.gg.common.config.Konf
import org.koin.core.Koin
import org.koin.core.qualifier.named
import java.io.File

object H2LocalDatabaseProvider : AbstractHikariProvider() {
    override fun getURL(config: Konf, koin: Koin): String {
        return "jdbc:h2:file:${koin.get<File>(named(KOIN_FOLDER_DATA)).absolutePath}${File.separator}" +
                (config[DatabaseConfigSpec.database].takeUnless { it.isNotBlank() } ?: "database")
    }
}