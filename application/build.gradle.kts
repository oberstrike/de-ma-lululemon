plugins {
    kotlin("plugin.allopen") version "1.6.10"
    id("io.quarkus")
    id("org.jetbrains.kotlin.plugin.noarg") version "1.6.10"
    id("org.kordamp.gradle.jandex")
}


val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

sourceSets {
    getByName("main").java.srcDirs("src/main/kotlin")
    getByName("test").java.srcDirs("src/test/kotlin")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-noarg:1.6.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    api(project ("::domain"))

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

allOpen {
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")

}

configure<org.jetbrains.kotlin.noarg.gradle.NoArgExtension> {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")

}