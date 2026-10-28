plugins {
    kotlin("multiplatform")
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
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}