group = rootProject.group
version = rootProject.version

dependencies {
    implementation("io.github.vovak:astminer:0.9.0") {
        exclude("org.slf4j", "slf4j-simple")
    }

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.0")
    implementation("me.tongfei:progressbar:0.9.2")
    testImplementation("io.mockk:mockk:1.12.3")
}

