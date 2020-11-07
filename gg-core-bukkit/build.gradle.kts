import kr.entree.spigradle.kotlin.spigot
import java.io.FileInputStream
import java.util.*

plugins {
    java
    maven
    `maven-publish`
    kotlin("jvm")
    id("kr.entree.spigradle")
    //id("org.jetbrains.dokka")
}

group = "de.phyrone"
version = System.getProperty("gg.version")

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(spigot())
    implementation(kotlin("stdlib-jdk8"))
    api(project(":gg-core-bukkit-common"))
    api(project(":gg-core-common"))
    api("me.lucko:commodore:1.9")
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
}
spigot {
    name = "GG-Core"
    apiVersion = "1.13"

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
        register("bukkit", MavenPublication::class.java) {
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