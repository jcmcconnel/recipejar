plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Core for shared later (data/persistence; Compose UI kept in composeApp)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${project.property("kotlinx.serialization.version")}")
                implementation("com.squareup.okio:okio:${project.property("okio.version")}")
            }
        }
        val desktopMain by getting {
            dependencies {
                // Desktop specific for shared if any
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val desktopTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
