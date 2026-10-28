@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    `maven-publish`
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
                implementation(project(":harmony-gl"))
                implementation(project(":harmony-wasm-utils"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.10.5")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
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