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

tasks {
    runIde {
        val dataset: String? by project
        val output: String? by project
        val config: String? by project
        args = listOfNotNull("psiminer", dataset, output, config)
        jvmArgs = listOf("-Djava.awt.headless=true")
        maxHeapSize = "20g"
    }
    register("runPSIMiner") {
        dependsOn(runIde)
    }
}
