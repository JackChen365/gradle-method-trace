// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath(fileTree(mapOf("dir" to "plugin_libs", "include" to listOf("*.jar"))))
    }
}
plugins {
    id("com.android.application") version "7.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.6.21" apply false
    id("org.jetbrains.kotlin.jvm") version "1.6.21" apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0" apply false
    id("com.android.library") version "7.2.1" apply false
    `java-gradle-plugin`
    `maven-publish`
}
repositories {
    mavenCentral()
    google()
    maven("https://plugins.gradle.org/m2/")
    maven("https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
}