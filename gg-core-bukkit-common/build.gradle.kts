import kr.entree.spigradle.kotlin.spigot

plugins {
    java
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