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

    implementation("com.github.Phyrone:universalitems:4ca58b7d99")

    implementation("com.github.deanveloper:SkullCreator:3b59220bc2")
    implementation("com.github.tr7zw.Item-NBT-API:item-nbt-api:2.3.1")
    implementation("fr.mrmicky:FastBoard:1.1.0")
    implementation("me.lucko:helper:5.6.2")
    implementation("me.lucko:helper-profiles:1.2.0")
    implementation("fr.minuskube.inv:smart-invs:1.2.7")
    api("io.github.classgraph:classgraph:4.8.86")
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