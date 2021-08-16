group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":psiminer-core"))

    implementation("com.github.ajalt:clikt:2.8.0")

    // Logging
    // SLF4J Facade
    implementation("org.slf4j:slf4j-api:1.7.25")
    // Then we bridge it to Log4j
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")
    // And set up the Log4j itself
    implementation("org.apache.logging.log4j:log4j-core:2.14.1")
}

tasks {
    runIde {
        val dataset: String? by project
        val output: String? by project
        val config: String? by project
        args = listOfNotNull("psiminer", dataset, output, config)
        jvmArgs = listOf(
            "-Djava.awt.headless=true", "-Djdk.module.illegalAccess.silent=true",
            "--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED", "-Xmx32G"
        )
        maxHeapSize = "32g"
    }
    register("runPSIMiner") {
        dependsOn(runIde)
    }
}
