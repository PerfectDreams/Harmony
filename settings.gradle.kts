plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "HarmonyFramework"

include(":harmony-wasm-utils")
include(":harmony-gl")
include(":harmony-opengl-lwjgl")
include(":harmony-webgl2")

// ===[ SAMPLES ]===
include(":samples:cube-lwjgl")