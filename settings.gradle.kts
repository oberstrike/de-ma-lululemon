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

include(":application", ":infrastructure", ":domain")

enableFeaturePreview("VERSION_CATALOGS")
