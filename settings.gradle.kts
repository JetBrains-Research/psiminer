rootProject.name = "psiminer"
include(
    "psiminer-cli",
    "psiminer-core"
)


val utilitiesRepo: String by settings
val utilitiesProjectName: String by settings

sourceControl {
    gitRepository(java.net.URI.create(utilitiesRepo)) {
        producesModule("$utilitiesProjectName:plugin-utilities-core")
    }
}
