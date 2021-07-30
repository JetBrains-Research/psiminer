group = "org.jetbrains.research.psiminer"
version = "1.0-SNAPSHOT"

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.1.3"
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
        maven(url = "https://dl.bintray.com/jetbrains/intellij-plugin-service")
        maven(url = "https://packages.jetbrains.team/maven/p/astminer/astminer")
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")

        testImplementation(platform("org.junit:junit-bom:5.7.2"))
        testImplementation("org.junit.jupiter:junit-jupiter")

        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.17.0")

        val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"
        implementation("$utilitiesProjectName:plugin-utilities-core") {
            version {
                branch = "main"
            }
        }
    }

    // See https://github.com/JetBrains/gradle-intellij-plugin/
    intellij {
        type.set("IC")
        version.set("212.4037.9-EAP-SNAPSHOT")
        plugins.set(listOf("java", "Kotlin", "android", "maven", "gradle", "Groovy"))
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
    }
}
