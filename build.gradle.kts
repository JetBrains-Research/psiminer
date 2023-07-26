group = "org.jetbrains.research.psiminer"
version = "2.0"

fun getProperty(key: String) =
    project.findProperty(key)?.toString() ?: error("Property $key in gradle.properties is not set")

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.9.0"
    id("io.gitlab.arturbosch.detekt") version "1.17.1"

    kotlin("jvm") version "1.7.21"
    kotlin("plugin.serialization") version "1.7.21"
}

allprojects {
    apply {
        plugin("java")
        plugin("kotlin")
        plugin("org.jetbrains.intellij")
        plugin("io.gitlab.arturbosch.detekt")
        plugin("kotlinx-serialization")
    }
    repositories {
        mavenCentral()
        maven(url = "https://packages.jetbrains.team/maven/p/astminer/astminer")
        maven("https://packages.jetbrains.team/maven/p/big-code/bigcode")
    }

    val utilitiesProjectVersion = "1.0"
    val utilitiesProjectId = "org.jetbrains.research"

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.21")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
        implementation("it.unimi.dsi:fastutil:8.5.9")
        implementation("org.jetbrains.research:plugin-utilities-core:1.0") {
            exclude("org.slf4j", "slf4j-simple")
            exclude("org.slf4j", "slf4j-api")
            exclude("org.slf4j", "slf4j")
        }

        testImplementation(platform("org.junit:junit-bom:5.9.0"))
        testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.18.1")

        implementation("$utilitiesProjectId:plugin-utilities-core:$utilitiesProjectVersion")
        implementation("$utilitiesProjectId:plugin-utilities-test:$utilitiesProjectVersion")
        implementation("$utilitiesProjectId:plugin-utilities-python:$utilitiesProjectVersion")
    }

    configurations.all {
        exclude("org.slf4j")
    }

    // See https://github.com/JetBrains/gradle-intellij-plugin/
    intellij {
        version.set(getProperty("platformVersion"))
        type.set(getProperty("platformType"))
        downloadSources.set(getProperty("platformDownloadSources").toBoolean())
        updateSinceUntilBuild.set(true)
        plugins.set(getProperty("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
    }

    detekt {
        allRules = true
        config = files(rootProject.projectDir.resolve("detekt.yml"))
        buildUponDefaultConfig = true
        parallel = true
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "17"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "17"
        }
        test {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
            }
            jvmArgs = listOf("-Djdk.module.illegalAccess.silent=true")
        }
    }
}
