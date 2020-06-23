import kr.entree.spigradle.kotlin.spigot
import java.io.FileInputStream
import java.util.*

plugins {
    java
    maven
    `maven-publish`
    kotlin("jvm")
    id("kr.entree.spigradle")
    id("org.jetbrains.dokka")
}

group = "de.phyrone"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

group = "de.phyrone"
version = System.getProperty("gg.version")

dependencies {
    compileOnly(spigot())
    implementation(project(":gg-core-common"))
    implementation(kotlin("stdlib-jdk8"))

    api("com.github.Phyrone:universalitems:4ca58b7d99")
    api("fr.mrmicky:FastParticles:v1.2.3")

    api("com.github.deanveloper:SkullCreator:3b59220bc2")
    api("com.github.tr7zw.Item-NBT-API:item-nbt-api:2.3.1")
    api("fr.mrmicky:FastBoard:1.1.0")
    api("me.lucko:helper:5.6.2")
    api("me.lucko:helper-profiles:1.2.0")
    api("fr.minuskube.inv:smart-invs:1.2.7")
    api("com.github.InventivetalentDev:ReflectionHelper:1.14.8-SNAPSHOT")
    api("com.github.InventivetalentDev.SpigetUpdater:bukkit:1.4.2-SNAPSHOT")
    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    spigotPluginYaml {
        enabled = false
    }
}
val publishPropertiesFile = File("./publish.properties")
val publishProperties by lazy {
    Properties().also { publishProperties ->
        if (publishPropertiesFile.exists()) {
            publishProperties.load(FileInputStream(publishPropertiesFile))
        }
    }
}
publishing {
    repositories {
        maven {
            setUrl(
                if ((version as String).endsWith("-SNAPSHOT"))
                    publishProperties["repo.url.snapshot"] as? String
                        ?: "https://repo.phyrone.de/repository/maven-release/" else
                    publishProperties["repo.url.release"] as? String
                        ?: "https://repo.phyrone.de/repository/maven-release/"
            )
            credentials {
                username = (publishProperties["repo.username"] as? String) ?: System.getenv("REPO_USER")
                password = (publishProperties["repo.password"] as? String) ?: System.getenv("REPO_PASSWORD")
            }
        }
    }
    publications {
        register("bukkit-common", MavenPublication::class.java) {
            from(components.getByName("java"))

            //shadow.component(this)
            //artifact(project(":gg-core-embeded").tasks.getByName("shadowJar"))
            pom {
                developers {
                    developer {
                        id.set("Phyrone")
                        name.set("Samuel Lauqa")
                        email.set("phyrone@phyrone.de")
                    }
                }
            }
        }
    }

}