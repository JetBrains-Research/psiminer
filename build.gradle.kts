group = "org.jetbrains.research.psiminer"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.5.0"

    id("idea")
    id("java")
    id("org.jetbrains.grammarkit") version "2020.3.1"
    id("org.jetbrains.intellij") version "0.7.3"

    kotlin("plugin.serialization") version "1.5.0"

    id("io.gitlab.arturbosch.detekt") version "1.17.0"
}

repositories {
    mavenCentral()
    maven(url = "https://dl.bintray.com/jetbrains/intellij-plugin-service")
    maven(url = "https://packages.jetbrains.team/maven/p/astminer/astminer")
}

dependencies {
    implementation("io.github.vovak:astminer:0.6.3")
    implementation("com.github.ajalt:clikt:2.8.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")

    testImplementation("junit:junit:4.11")
    testImplementation(kotlin("test-junit"))

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.17.0")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2021.1"
    setPlugins("java")
}

detekt {
    allRules = true
    config = files("detekt.yml")
    buildUponDefaultConfig = true
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
