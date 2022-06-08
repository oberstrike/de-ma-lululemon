plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.mavenPublish) apply false
    id("org.kordamp.gradle.jandex") apply false
}


//apply group and version to all projects
allprojects  {
    group = property("GROUP") as String
    version = property("VERSION_NAME") as String

    repositories {
        mavenCentral()
    }


}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.vanniktech.maven.publish")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
        kotlinOptions.javaParameters = true
    }

}
