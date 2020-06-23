import kr.entree.spigradle.kotlin.spigot

plugins {
    java
    maven
    kotlin("jvm")
    id("kr.entree.spigradle")
    id("org.jetbrains.dokka")
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
    api("me.lucko:commodore:1.5")
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