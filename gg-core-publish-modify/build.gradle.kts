import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import java.io.FileInputStream
import java.util.*

plugins {
    java
    maven
    idea
    `maven-publish`

    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

group = "de.phyrone"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(rootProject)
    testCompile("junit", "junit", "4.12")
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
    shadowJar {
        classifier = null
        baseName = "GG-Core"

        mergeServiceFiles()
        //transform(ApacheLicenseResourceTransformer())
        //transform(ApacheNoticeResourceTransformer())
        //transform(ManifestAppenderTransformer())
        transform(Log4j2PluginsCacheFileTransformer())
        dependencies {
            this.exclude(dependency("org.yaml:snakeyaml"))
            //this.exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8"))
            //this.exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
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
            "module-info.class",
            "kotlin/**",
            "org/jetbrains/annotations",
            "kotlinx/**"
        )
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
                    "https://repo.phyrone.de/repository/maven-snapshot/" else
                    "https://repo.phyrone.de/repository/maven-release/"
            )
            credentials {
                username = (publishProperties["repo.username"] as? String) ?: System.getenv("REPO_USER")
                password = (publishProperties["repo.password"] as? String) ?: System.getenv("REPO_PASSWORD")
            }
        }
    }
    publications {

        register("core", MavenPublication::class.java) {
            artifactId = "gg-core"
            project.shadow.component(this)

            //artifact(dependencies.implementation(dependencies.kotlin("stdlib-jdk8")))
            //from(components.getByName("java"))

            pom {
                developers {
                    developer {
                        id.set("Phyrone")
                        name.set("Samuel Lauqa")
                        email.set("phyrone@phyrone.de")
                    }
                }
                withXml {
                    val cNode = this.asNode()
                    val dependenciesName = "dependencies"
                    val dependenciesNode = (
                            (cNode.get(dependenciesName)
                                    as? groovy.util.NodeList)?.firstOrNull()
                                    as? groovy.util.Node)
                        ?: cNode.appendNode(dependenciesName)

                    listOf(
                        "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.72",
                        "org.spigotmc:spigot-api:1.15.2-R0.1-SNAPSHOT"
                    ).forEach { dep ->
                        val splited = dep.split(":")
                        val depNode = dependenciesNode.appendNode("dependency")
                        depNode.appendNode("groupId", splited.component1())
                        depNode.appendNode("artifactId", splited.component2())
                        depNode.appendNode("version", splited.component3())
                        depNode.appendNode("scope", "runtime")
                    }

                }
            }
        }
    }
}