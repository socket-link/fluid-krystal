import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        tvosX64(),
        tvosArm64(),
        tvosSimulatorArm64(),
        watchosX64(),
        watchosArm64(),
        watchosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    macosX64()
    macosArm64()
    linuxX64()
    linuxArm64()
    mingwX64()

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside the browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }
    
    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val appleMain by creating {
            dependsOn(commonMain.get())
        }

        val desktopMain by creating {
            dependsOn(commonMain.get())
        }

        iosX64Main.get().dependsOn(appleMain)
        iosArm64Main.get().dependsOn(appleMain)
        iosSimulatorArm64Main.get().dependsOn(appleMain)
        tvosX64Main.get().dependsOn(appleMain)
        tvosArm64Main.get().dependsOn(appleMain)
        tvosSimulatorArm64Main.get().dependsOn(appleMain)
        watchosX64Main.get().dependsOn(appleMain)
        watchosArm64Main.get().dependsOn(appleMain)
        watchosSimulatorArm64Main.get().dependsOn(appleMain)
        macosX64Main.get().dependsOn(appleMain)
        macosArm64Main.get().dependsOn(appleMain)

        macosX64Main.get().dependsOn(desktopMain)
        macosArm64Main.get().dependsOn(desktopMain)
        linuxX64Main.get().dependsOn(desktopMain)
        linuxArm64Main.get().dependsOn(desktopMain)
        mingwX64Main.get().dependsOn(desktopMain)
    }
}

android {
    namespace = "link.socket.krystal.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
