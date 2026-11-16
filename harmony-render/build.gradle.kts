plugins {
    kotlin("multiplatform")
    `maven-publish`
}

kotlin {
    jvm()

    js {
        browser()
        binaries.library()
    }

    wasmJs {
        browser()
        binaries.library()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":harmony-gl"))
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