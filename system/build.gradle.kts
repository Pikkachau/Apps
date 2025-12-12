plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }

    maven("https://repo.extendedclip.com/releases/")
    maven("https://jitpack.io")
    maven("https://repo.artillex-studios.com/releases") // <--- Add this
}

dependencies {
    implementation(project(":block"))

    //Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    //Spigot
    compileOnly("org.spigotmc:spigot-api:1.21.6-R0.1-SNAPSHOT")

    //Plugins
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    //Dagger
    implementation("com.google.dagger:dagger:2.56.2")
    ksp("com.google.dagger:dagger-compiler:2.56.2")
    compileOnly("com.artillexstudios:AxPlayerWarps:1.11.2") // <--- Add this
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
