plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    jvmToolchain(21)
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.example.kmpdemo.MainKt"
    }
}
