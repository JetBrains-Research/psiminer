group = "org.jetbrains.research.psiminer"
version = "1.0-SNAPSHOT"

plugins {
    java
    kotlin("jvm") version "1.5.0"

    id("org.jetbrains.grammarkit") version "2020.3.1"
    id("org.jetbrains.intellij") version "0.7.3"
    id("io.gitlab.arturbosch.detekt") version "1.17.0"
}

allprojects {
    apply {
        plugin("java")
        plugin("kotlin")
        plugin("org.jetbrains.grammarkit")
        plugin("org.jetbrains.intellij")
        plugin("io.gitlab.arturbosch.detekt")
    }
    repositories {
        mavenCentral()
        maven(url = "https://dl.bintray.com/jetbrains/intellij-plugin-service")
        maven(url = "https://packages.jetbrains.team/maven/p/astminer/astminer")
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.0")

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
