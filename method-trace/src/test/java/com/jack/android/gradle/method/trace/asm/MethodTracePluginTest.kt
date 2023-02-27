package com.jack.android.gradle.method.trace.asm

import com.github.jackchen.gradle.test.toolkit.GradlePluginTest
import com.github.jackchen.gradle.test.toolkit.ext.TestVersion
import com.github.jackchen.gradle.test.toolkit.ext.TestWithCache
import com.github.jackchen.gradle.test.toolkit.testdsl.TestProjectIntegration
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

internal class MethodTracePluginTest : GradlePluginTest() {
    private fun testProjectSetup(closure: TestProjectIntegration.TestProject.() -> Unit) {
        kotlinAndroidTemplate {
            template {
                plugins {
                    id("method.trace").version("1.0.0-SNAPSHOT")
                }
                dependencies {
                    implementation("androidx.core:core-ktx:1.7.0")
                    implementation("androidx.appcompat:appcompat:1.4.1")
                }
            }
            project {
                apply(closure)
            }
        }
    }

    @Test
    @TestWithCache(true)
    @TestVersion(androidVersion = "7.2.0", gradleVersion = "7.4.1")
    fun buildTest() {
        testProjectSetup {
            file("app/build.gradle.kts"){
                """
                |plugins {
                |	id("com.android.application")
                |	id("org.jetbrains.kotlin.android")
                |	id("method.trace")
                |}
                |android {
                |    compileSdk = 31
                |    defaultConfig {
                |        applicationId = "com.android.test"
                |        minSdk = 21
                |        targetSdk = 31
                |        versionCode = 1
                |        versionName = "1.0"
                |
                |        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                |    }
                |    compileOptions {
                |        sourceCompatibility = JavaVersion.VERSION_1_8
                |        targetCompatibility = JavaVersion.VERSION_1_8
                |    }
                |    kotlinOptions {
                |        jvmTarget = "1.8"
                |    }
                |}
                |methodTrace {
                |   methodTrace {
                |       including("com.android.test.MainActivity")
                |   }
                |}
                |dependencies {
                |	implementation("androidx.core:core-ktx:1.7.0")
                |	implementation("androidx.appcompat:appcompat:1.4.1")
                |}
                """.trimMargin()
            }
            file("app/src/main/kotlin/com/android/test/MainActivity.kt"){
                """
                |package com.android.test
                |import androidx.appcompat.app.AppCompatActivity
                |import android.os.Bundle
                |class MainActivity : AppCompatActivity(){
                |    override fun onCreate(savedInstanceState: Bundle?) {
                |        super.onCreate(savedInstanceState)
                |        println("onCreate.")
                |    }
                |}
                """.trimMargin()
            }
            build(":app:assembleDebug") {
                Assertions.assertEquals(TaskOutcome.SUCCESS, task(":app:assembleDebug")?.outcome)
            }
        }
    }
}
