group = "org.jetbrains.research.psiminer"
version = "1.0-SNAPSHOT"

plugins {
    id("java")
    id("idea")
    id("org.jetbrains.grammarkit") version "2020.1"
    id("org.jetbrains.intellij") version "0.4.21"
    id("io.gitlab.arturbosch.detekt") version "1.10.0"
    kotlin("jvm") version "1.3.72"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/egor-bogomolov/astminer")
    maven(url = "https://dl.bintray.com/jetbrains/intellij-plugin-service")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.github.vovak.astminer:astminer-dev:1.319")
    implementation("me.tongfei:progressbar:0.8.1")
    implementation("com.github.ajalt:clikt:2.8.0")

    testImplementation("junit:junit:4.11")
    testImplementation(kotlin("test-junit"))

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.10.0")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2020.2"
    setPlugins("java")
}

detekt {
    failFast = true // fail build on any finding
    buildUponDefaultConfig = true // preconfigure defaults
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
        args = listOfNotNull("psiminer", "--dataset", dataset, "--output", output)
        jvmArgs = listOf("-Djava.awt.headless=true")
    }
    register("extractPSIPaths") {
        dependsOn(runIde)
    }
}
