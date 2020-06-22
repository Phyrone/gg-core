package de.phyrone.gg.database.provider

import de.phyrone.gg.common.config.Konf
import org.koin.core.Koin
import javax.sql.DataSource

interface DatasourceProvider {
    val names: Set<String>
    operator fun get(config: Konf, koin: Koin): DataSource
}