plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    jvm("desktop")

    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // iOS framework scaffolding (Phase 1A product UI still deferred)
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "RecipeJarShared"
            isStatic = true
        }
    }

    // Align with composeApp / KCEF (Java 17+)
    jvmToolchain(17)

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
        val androidMain by getting {
            dependencies {
                // Android FS/WebView adapters land in Phase 1A; common domain compiles as-is
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
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

android {
    namespace = "org.recipejar.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
