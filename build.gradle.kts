plugins {
    kotlin("jvm") version "2.4.21"
}

subprojects {
    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "PerfectDreams"
                    url = uri("https://repo.perfectdreams.net/")
                    credentials(PasswordCredentials::class)
                }
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}