pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        mavenLocal()
        maven("https://plugins.gradle.org/m2/")
        maven("https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")
rootProject.name = "gradle-method-trace"
rootProject.buildFileName = "build.gradle.kts"
//include(":app")
include(":app")
include(":method-trace-runtime")
include(":method-trace")
