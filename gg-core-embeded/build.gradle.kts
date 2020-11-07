import kr.entree.spigradle.kotlin.bungeecord
import kr.entree.spigradle.kotlin.spigot
import java.io.FileInputStream
import java.util.*

plugins {
    java
    maven
    `maven-publish`
    id("kr.entree.spigradle")
    //id("org.jetbrains.dokka")
    id("com.github.johnrengelman.shadow")
}

group = "de.phyrone"
version = System.getProperty("gg.version")

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit", "junit", "4.12")
    compileOnly(spigot())
    compileOnly(bungeecord())
    implementation("org.jetbrains:annotations:20.1.0")
    compileOnly(project.rootProject)
    compileOnly(project(":gg-core-bukkit-common"))
    compileOnly(project(":gg-core-bukkit"))
    compileOnly(project(":gg-core-common"))
}
tasks {
    //spigotPluginYaml { enabled = false }
    shadowJar {
        classifier = null
    }
}
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
val publishPropertiesFile = File("./publish.properties")
val publishProperties by lazy {
    Properties().also { publishProperties ->
        if (publishPropertiesFile.exists()) {
            publishProperties.load(FileInputStream(publishPropertiesFile))
        }
    }
}
tasks.generateSpigotDescription{
    enabled = false
}
publishing {
    repositories {
        maven {
            setUrl(
                if ((version as String).endsWith("-SNAPSHOT"))
                    publishProperties["repo.url.snapshot"] as? String
                        ?: "https://repo.phyrone.de/repository/maven-snapshot/" else
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
        register("embeded", MavenPublication::class.java) {
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

