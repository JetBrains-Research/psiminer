group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":psiminer-core"))

    implementation("com.github.ajalt:clikt:2.8.0")
}

tasks {
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
