plugins {
    java
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "7.0.4"
    id("com.github.spotbugs") version "6.1.7"
    checkstyle
}

group = "com.mediaserver"
version = "1.0.0"
description = "Spring Boot Media Server with Mega.nz integration"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val testcontainersVersion = "1.20.4"
val mapstructVersion = "1.6.3"
val lombokVersion = "1.18.36"

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    // Database
    implementation("org.liquibase:liquibase-core")
    runtimeOnly("org.postgresql:postgresql")

    // JSON Processing
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Utilities
    implementation("commons-io:commons-io:2.21.0")
    implementation("org.mapstruct:mapstruct:$mapstructVersion")

    // Lombok + MapStruct (annotation processors must be in correct order)
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    // Test dependencies for Lombok
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
}

// Spotless (Code Formatting)
spotless {
    java {
        target("src/main/java/**/*.java", "src/test/java/**/*.java")
        googleJavaFormat("1.28.0").aosp().reflowLongStrings()
        formatAnnotations()
        removeUnusedImports()
        importOrder("", "javax|jakarta", "java", "\\#")
        toggleOffOn()
    }
}

// Checkstyle
checkstyle {
    toolVersion = "10.21.4"
    configFile = file("checkstyle.xml")
    isShowViolations = true
    isIgnoreFailures = false
}

tasks.withType<Checkstyle> {
    reports {
        xml.required = true
        html.required = true
    }
}

// SpotBugs - Updated for Java 21 compatibility
spotbugs {
    effort = com.github.spotbugs.snom.Effort.MAX
    reportLevel = com.github.spotbugs.snom.Confidence.MEDIUM
    excludeFilter = file("spotbugs-exclude.xml")
    showProgress = true
}

// Configure auxclasspath for all SpotBugs tasks to resolve Spring/dependency classes
tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    auxClassPaths.from(sourceSets["main"].compileClasspath)
    reports.create("xml") { required = true }
    reports.create("html") { required = true }
}

tasks.spotbugsTest {
    auxClassPaths.from(sourceSets["test"].compileClasspath)
}

// Test Configuration
tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")

    testLogging {
        events("passed", "skipped", "failed")
    }
}

// Exclude Lombok from bootJar
tasks.bootJar {
    exclude("org/projectlombok/**")
}
