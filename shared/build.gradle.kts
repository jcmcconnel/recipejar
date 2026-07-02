plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
}

kotlin {
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Core for shared later
                implementation(compose.runtime)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${project.property("kotlinx.serialization.version")}")
                implementation("com.squareup.okio:okio:${project.property("okio.version")}")
            }
        }
        val desktopMain by getting {
            dependencies {
                // Desktop specific for shared if any
            }
        }
    }
}