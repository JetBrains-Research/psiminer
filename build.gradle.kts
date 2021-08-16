group = "org.jetbrains.research.psiminer"
version = "2.0"

fun getProperty(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.1.4"
    id("io.gitlab.arturbosch.detekt") version "1.17.0"

    kotlin("jvm") version "1.5.21"
    kotlin("plugin.serialization") version "1.5.0"
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
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

        testImplementation(platform("org.junit:junit-bom:5.7.2"))
        testImplementation("org.junit.jupiter:junit-jupiter")

        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.17.0")

        val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"
        implementation("$utilitiesProjectName:plugin-utilities-core") {
            version {
                branch = "open_repo"
            }
            exclude("org.slf4j", "slf4j-simple")
            exclude("org.slf4j", "slf4j-api")
            exclude("org.slf4j", "slf4j")
        }
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
            kotlinOptions.jvmTarget = "11"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "11"
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
