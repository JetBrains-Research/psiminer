group = "org.jetbrains.research.psiminer"
version = "1.0-SNAPSHOT"

plugins {
    id("java")
    id("idea")
    id("org.jetbrains.grammarkit") version "2020.3.1"
    id("org.jetbrains.intellij") version "0.6.5"
    id("io.gitlab.arturbosch.detekt") version "1.17.0"
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.10"
}

repositories {
    mavenCentral()
    maven(url = "https://dl.bintray.com/jetbrains/intellij-plugin-service")
    maven(url = "https://packages.jetbrains.team/maven/p/astminer/astminer")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.github.vovak:astminer:0.6.3")
    implementation("com.github.ajalt:clikt:2.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test-junit"))

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.17.0")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2020.3"
    setPlugins("java")
}

detekt {
    failFast = true
    config = files("detekt.yml")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
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
