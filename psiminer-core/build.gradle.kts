group = rootProject.group
version = rootProject.version

dependencies {
    implementation("io.github.vovak:astminer:0.9.0") {
        exclude("org.slf4j", "slf4j-simple")
        exclude("net.java.dev.jna", "jna")
    }

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.21")
    implementation("me.tongfei:progressbar:0.9.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation("io.mockk:mockk:1.13.2")
}

