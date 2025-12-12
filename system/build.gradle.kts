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
    maven("https://repo.artillex-studios.com/releases")
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
    testImplementation(kotlin("test"))
    // Define the path relative to the PROJECT directory (system/)
    val jarFile = layout.projectDirectory.file("libs/AxPlayerWarps-1.11.2.jar").asFile

    if (jarFile.exists()) {
        println("SUCCESS: Found AxPlayerWarps jar at: ${jarFile.absolutePath}")
        compileOnly(files(jarFile))
    } else {
        // This will show up in your GitHub Actions logs in RED
        println("ERROR: Jar file NOT found at: ${jarFile.absolutePath}")
        println("       Please check that the file is in 'system/libs/' and the name matches exactly.")
        
        // Fallback: Check if it's in the root libs folder just in case
        val rootJar = rootProject.layout.projectDirectory.file("libs/AxPlayerWarps-1.11.2.jar").asFile
        if (rootJar.exists()) {
             println("SUCCESS: Found AxPlayerWarps jar in ROOT libs.")
             compileOnly(files(rootJar))
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
