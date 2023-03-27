import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val pluginGroup = "com.github.jackchen.method.analysis"
group = pluginGroup
version = "1.0.0-SNAPSHOT"

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

gradlePlugin {
    plugins {
        register("method-trace") {
            id = "method.trace"
            displayName = "MethodTracePlugin"
            description = "This is a plugin for us to trace the method."
            implementationClass = "com.jack.android.gradle.method.trace.MethodTracePlugin"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

configurations.compileOnly.configure { isCanBeResolved = true }
tasks.withType<PluginUnderTestMetadata>().configureEach {
    pluginClasspath.from(configurations.compileOnly)
}

// Test tasks loods plugin from local maven repository
tasks.named("test").configure {
    dependsOn("publishToMavenLocal")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.java.asm)
    compileOnly(libs.java.asm.commons)

    testImplementation(gradleTestKit())
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.gradle.test.toolkit)
    testImplementation(projects.methodTraceRuntime)
    testImplementation("org.ow2.asm:asm:6.0")
    testImplementation("org.ow2.asm:asm-commons:6.0")
    testImplementation("org.ow2.asm:asm-util:6.0")
}