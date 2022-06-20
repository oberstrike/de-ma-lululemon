plugins {
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

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    api(project ("::api"))
    api(project ("::domain"))

    testImplementation(libs.junit5Jupiter)
    testImplementation(libs.junit5JupiterApi)
    testImplementation(libs.junit5JupiterParams)
    testRuntimeOnly(libs.junit5JupiterEngine)
    testImplementation(libs.kluent)
    testImplementation(libs.mockk)
}

