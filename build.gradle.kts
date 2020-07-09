
group = "org.jetbrains.research.psiminer"
version = "1.0-SNAPSHOT"

// Specify path to local build of IDEA
val localPathToIdea = "<PATH>"

plugins {
    id("java")
    id("idea")
    id("org.jetbrains.grammarkit") version "2020.1"
    id("org.jetbrains.intellij") version "0.4.21"
    kotlin("jvm") version "1.3.72"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/egor-bogomolov/astminer")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://dl.bintray.com/jetbrains/intellij-plugin-service")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.github.vovak.astminer:astminer:0.5")
    implementation("net.openhft:chronicle-map:3.19.31")

    testImplementation("junit:junit:4.11")
    testImplementation(kotlin("test-junit"))
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    type = "IC"
    localPath = localPathToIdea
    setPlugins("java")
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      Add change notes here.<br>
      <em>most HTML tags may be used</em>""")
}