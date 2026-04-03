import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("com.vanniktech.maven.publish") version "0.32.0"
    signing
}

group = "link.socket.krystal"
version = "0.2.0"

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "KrystalCore"
            isStatic = true
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.haze)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val skikoMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation("org.jetbrains.skiko:skiko:0.9.4.2")
            }
        }

        val iosMain by creating {
            dependsOn(commonMain.get())
        }
        iosX64Main.get().dependsOn(iosMain)
        iosArm64Main.get().dependsOn(iosMain)
        iosSimulatorArm64Main.get().dependsOn(iosMain)

        androidMain.dependencies {
            implementation("androidx.core:core-ktx:1.16.0")
        }

        val desktopMain by getting {
            dependsOn(skikoMain)
        }

        wasmJsMain {
            dependsOn(skikoMain)
        }
    }
}

android {
    namespace = "link.socket.krystal.core"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

signing {
    useGpgCmd()
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set("Fluid Krystal")
        description.set("Kotlin Multiplatform glass-effect rendering library")
        url.set("https://github.com/socket-link/fluid-krystal")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("socket")
                name.set("Socket")
                url.set("https://socket.link")
            }
        }
        scm {
            url.set("https://github.com/socket-link/fluid-krystal")
            connection.set("scm:git:git://github.com/socket-link/fluid-krystal.git")
            developerConnection.set("scm:git:ssh://github.com/socket-link/fluid-krystal.git")
        }
    }
}
