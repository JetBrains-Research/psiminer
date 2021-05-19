group = rootProject.group
version = rootProject.version

plugins {
    kotlin("plugin.serialization") version "1.5.0"
}

dependencies {
    implementation(project(":psiminer-core"))

    implementation("com.github.ajalt:clikt:2.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
}
