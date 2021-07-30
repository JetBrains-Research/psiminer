rootProject.name = "psiminer"
include(
    "psiminer-cli",
    "psiminer-core"
)

val utilitiesRepo = "https://github.com/Furetur/plugin-utilities.git"
val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"

sourceControl {
    gitRepository(java.net.URI.create(utilitiesRepo)) {
        producesModule("$utilitiesProjectName:plugin-utilities-core")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        maven(url = "https://nexus.gluonhq.com/nexus/content/repositories/releases")
    }
}
