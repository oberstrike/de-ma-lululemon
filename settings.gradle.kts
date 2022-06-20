rootProject.name = "pricetracker"

pluginManagement {
    val quarkusPluginVersion: String by settings
    val quarkusPluginId: String by settings
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
    plugins {
        id(quarkusPluginId) version quarkusPluginVersion
        id("org.kordamp.gradle.jandex") version "0.11.0"
    }
}

include(":api", ":app", ":domain", ":impl")

enableFeaturePreview("VERSION_CATALOGS")
