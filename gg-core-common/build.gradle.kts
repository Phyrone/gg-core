plugins {
    java
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

group = "de.phyrone"
version = System.getProperty("gg.version")

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("junit", "junit", "4.12")

    implementation("org.apache.maven", "maven-resolver-provider", "3.5.0")

    listOf(
        "maven-resolver-api",
        "maven-resolver-spi",
        "maven-resolver-util",
        "maven-resolver-impl",
        "maven-resolver-connector-basic",
        "maven-resolver-transport-file",
        "maven-resolver-transport-http"
    ).forEach { name ->
        implementation("org.apache.maven.resolver", name, "1.1.1")
    }
    val koin_version = "2.1.6"
    api("org.koin:koin-core:$koin_version")
    api("org.koin:koin-core-ext:$koin_version")
    testImplementation("org.koin:koin-test:$koin_version")

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