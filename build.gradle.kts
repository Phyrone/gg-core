import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import kr.entree.spigradle.kotlin.*
import java.io.FileInputStream
import java.util.*

plugins {
    java
    maven
    `maven-publish`
    idea
    id("kr.entree.spigradle") version "2.2.3"
    //id("org.jetbrains.dokka") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("de.undercouch.download") version "1.2"
    kotlin("jvm") version "1.4.10"

}

group = "de.phyrone"
version = System.getProperty("gg.version")

repositories {
    mavenCentral()
}
allprojects {
    apply(plugin = "maven-publish")
    repositories {
        spigotmc()
        papermc()
        bungeecord()
        jitpack()
        mavenCentral()
        mavenLocal()
        jcenter()
        codemc()
        maven("https://jcenter.bintray.com")
        maven("https://libraries.minecraft.net")
        maven("https://raw.github.com/rjenkinsjr/maven2/repo")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.inventivetalent.org/content/groups/public/")
        maven("https://dl.cloudsmith.io/public/anand-beh/arim-repo/maven/")

    }

}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    testImplementation("junit", "junit", "4.12")
    api(project(":gg-core-common"))
    api(project(":gg-core-bukkit"))
    api(project(":gg-core-bukkit-common"))
    compileOnly(spigot())
    compileOnly(bungeecord())
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
val spigotVersions = listOf("1.8.8", "1.9.4", "1.10.2", "1.11.2", "1.12.2", "1.13.2", "1.14.4", "1.15.2")
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    /*
    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/docs"
        subProjects = listOf(
            "gg-core-common",
            "gg-core-bukkit-common",
            "gg-core-bukkit"
        )
        //subProjects = subprojects.map { project -> project.name }

    }
     */
    shadowJar {
        classifier = ""
        baseName = "GG-Core"
        version = null

        mergeServiceFiles()
        //transform(ApacheLicenseResourceTransformer())
        //transform(ApacheNoticeResourceTransformer())
        //transform(ManifestAppenderTransformer())
        transform(Log4j2PluginsCacheFileTransformer())
        dependencies {
            this.exclude(dependency("org.yaml:snakeyaml"))
        }
        exclude(
            //"**/*.kotlin_metadata",
            //"**/*.kotlin_module",
            //"**/*.kotlin_builtins",
            "META-INF/maven/**",
            "META-INF/versions/**",
            "META-INF/proguard/**",
            "META-INF/plexus/**",
            "META-INF/sisu/**",
            "META-INF/DEPENDENCIES",
            "META-INF/NOTICE",
            "META-INF/NOTICE.txt",
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt",
            "licenses/**",
            "LICENSE",
            "LICENSE.txt",
            "module-info.class"
        )
    }

    //spigotPluginYaml { enabled = false }
    create("first-setup") {

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
        register("core", MavenPublication::class.java) {
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

