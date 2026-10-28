plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

val lwjglVersion = "3.4.3"
val lwjglNatives = findProperty("lwjglNativesPlatform") ?: "natives-linux"

dependencies {
    api(project(":harmony-opengl-lwjgl"))

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjgl:lwjgl-sdl")
    implementation("org.lwjgl:lwjgl-stb")
    implementation("org.lwjgl:lwjgl-tinyfd")
    implementation("org.lwjgl:lwjgl::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-opengl::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-sdl::$lwjglNatives")
    implementation("org.joml:joml:1.10.9")
}