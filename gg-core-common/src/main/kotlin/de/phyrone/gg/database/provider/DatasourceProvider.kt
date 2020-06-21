package de.phyrone.gg.database.provider

import de.phyrone.gg.common.config.Konf
import org.koin.core.Koin
import javax.sql.DataSource

interface DatasourceProvider {
    operator fun get(config: Konf, koin: Koin): DataSource
}