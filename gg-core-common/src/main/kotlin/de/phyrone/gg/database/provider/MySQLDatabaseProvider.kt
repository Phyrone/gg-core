package de.phyrone.gg.database.provider

import de.phyrone.gg.KOIN_PLUGIN_NAME
import de.phyrone.gg.common.config.DatabaseConfigSpec
import de.phyrone.gg.common.config.Konf
import org.koin.core.Koin
import org.koin.core.qualifier.named

object MySQLDatabaseProvider : AbstractHikariProvider() {
    override val driver: String? = "com.mysql.jdbc.Driver "
    override fun getURL(config: Konf, koin: Koin): String {
        return "jdbc:mysql://${config[DatabaseConfigSpec.hosts].joinToString(",") { hostAndPort -> "${hostAndPort.host}:${hostAndPort.port}" }}/" +
                (config[DatabaseConfigSpec.database].takeUnless { it.isBlank() } ?: koin.get(named(KOIN_PLUGIN_NAME)))
    }

    override val names: Set<String> = setOf("mysql", "mariadb"/* TODO(add external mariadb provider) */)
}