group = "org.jetbrains.research.psiminer"
version = "1.0-SNAPSHOT"

plugins {
    id("java")
    id("org.jetbrains.intellij") version "0.7.3"
    id("io.gitlab.arturbosch.detekt") version "1.17.0"

    kotlin("jvm") version "1.5.10"
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

        testImplementation("junit:junit:4.13")
        testImplementation(kotlin("test-junit"))

        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.17.0")
    }

    // See https://github.com/JetBrains/gradle-intellij-plugin/
    intellij {
        type = "IC"
        version = "2021.1"
        setPlugins("java", "org.jetbrains.kotlin:211-1.5.0-release-759-IJ6693.72")
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
