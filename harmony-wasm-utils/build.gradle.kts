@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        browser()
        binaries.library()
    }

    wasmJs {
        browser()
        binaries.library()
    }

    sourceSets {
        webMain {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.10.0")
            }
        }
    }
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}