package de.phyrone.gg.common.config

import com.uchuhimo.konf.ConfigSpec

object DatabaseConfigSpec : ConfigSpec("database") {
    val provider by optional("h2", "provider")
    val url by optional("", "url")

    val hosts by optional(listOf(HostAndPort("127.0.0.1", 0)), "hosts")
    val database by optional("", "database")

    object Credentials : ConfigSpec("credentials") {
        val userName by optional("", "username")
        val password by optional("", "password")
    }


    object Pool : ConfigSpec("pool") {
        val min by optional(2, "minimum-idle")
        val max by optional(10, "maximum-pool-size")
        val maximumLifetime by optional(1800000L, "maximum-lifetime")
        val connectionTimeout by optional(5000L, "connection-timeout")

    }


}