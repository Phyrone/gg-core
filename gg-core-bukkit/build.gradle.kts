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
    implementation(project(":gg-core-bukkit-common"))
    implementation(project(":gg-core-common"))
    implementation("me.lucko:commodore:1.5")
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