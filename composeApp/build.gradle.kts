import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
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
                // api: desktop Main needs KCEF types for WebView bootstrap
                api("io.github.kevinnzou:compose-webview-multiplatform:${project.property("webview.version")}")
                implementation("com.squareup.okio:okio:${project.property("okio.version")}")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "recipejar.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "RecipeJar"
            packageVersion = "1.0.0"
        }
    }
}

// KCEF / JCEF requires these opens (see compose-webview-multiplatform README.desktop.md)
afterEvaluate {
    tasks.withType<JavaExec>().configureEach {
        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED")
        if (System.getProperty("os.name").contains("Mac", ignoreCase = true)) {
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }
    }
}
