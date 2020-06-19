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
            //this.exclude(dependency(""))

        }
        exclude(
            "**/*.kotlin_metadata",
            "**/*.kotlin_module",
            "**/*.kotlin_builtins",
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

            //from(components.getByName("java"))
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