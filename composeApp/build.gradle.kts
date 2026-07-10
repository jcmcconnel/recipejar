import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
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

    jvmToolchain(11)

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

afterEvaluate {
    tasks.withType<JavaExec>().configureEach {
        jvmArgs(kcefJvmOpens)
    }
}
