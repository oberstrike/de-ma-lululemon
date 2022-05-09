
plugins {
    kotlin("jvm") version "1.6.20"
    kotlin("plugin.allopen") version "1.6.20"
    id("io.quarkus")
}


repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-quartz")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-mongodb-panache-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("it.skrape:skrapeit:1.2.1")
  //  implementation("it.skrape:skrapeit-browser-fetcher:1.2.1")
    implementation("it.skrape:skrapeit-http-fetcher:1.2.1")
    implementation("io.quarkus:quarkus-arc")
    //implementation("org.mockito:mockito-junit-jupiter:4.5.1")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.kotest:kotest-runner-junit5:5.3.0")
    implementation("io.quarkiverse.mockk:quarkus-junit5-mockk:1.1.1")

}

group = "de.ma.lululemon"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    kotlinOptions.javaParameters = true
}
