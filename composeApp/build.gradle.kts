import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
    id("com.android.application")
}

// KCEF / JCEF reflective access (compose-webview-multiplatform README.desktop.md)
private val kcefJvmOpens = buildList {
    addAll(
        listOf(
            "--add-opens", "java.desktop/sun.awt=ALL-UNNAMED",
            "--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED",
        )
    )
    if (System.getProperty("os.name").contains("Mac", ignoreCase = true)) {
        addAll(
            listOf(
                "--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED",
                "--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
            )
        )
    }
}

kotlin {
    jvm("desktop")

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // iOS app host (phone + iPad Simulator / device families via single binary)
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    // KCEF (compose-webview-multiplatform desktop) is bytecode 61 / Java 17+
    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${project.property("kotlinx.serialization.version")}")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                // Same module source set: implementation is enough for Main + WebView actual
                implementation("io.github.kevinnzou:compose-webview-multiplatform:${project.property("webview.version")}")
                implementation("com.squareup.okio:okio:${project.property("okio.version")}")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation("androidx.activity:activity-compose:1.9.3")
                implementation("androidx.appcompat:appcompat:1.7.0")
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                // Compose UIKit host for iPhone / iPad
            }
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}

android {
    namespace = "org.recipejar.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.recipejar.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

compose.desktop {
    application {
        mainClass = "recipejar.MainKt"
        // Packaged DMG/MSI/Deb and release runs need the same opens as Gradle JavaExec
        jvmArgs += kcefJvmOpens

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "RecipeJar"
            packageVersion = "1.0.0"
            description = "Local offline recipe organizer"
            vendor = "RecipeJar"
            copyright = "© RecipeJar contributors"
            // Packaged app inherits jvmArgs above (KCEF --add-opens for all OSes + mac extras).

            macOS {
                bundleID = "org.recipejar.app"
                // Screen menu bar / dock title use packageName; apple.awt.application.name set at runtime.
            }
            windows {
                menuGroup = "RecipeJar"
                // Per-user install is friendlier for recipe-folder workflows.
                dirChooser = true
            }
            linux {
                menuGroup = "Office"
                appCategory = "Office"
            }
        }
    }
}

// Ensure :composeApp:run (and other JavaExec) use JDK 17 even when system `java` is 11.
afterEvaluate {
    val toolchains = project.extensions.getByType(JavaToolchainService::class.java)
    val jdk17 = toolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    tasks.withType<JavaExec>().configureEach {
        javaLauncher.set(jdk17)
        jvmArgs(kcefJvmOpens)
    }
}
