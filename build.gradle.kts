plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // TUI Libraries
    implementation("com.googlecode.lanterna:lanterna:3.1.1")
    implementation("com.github.ajalt.mordant:mordant:2.7.2")

    // JSON serialization for save/load
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}

kotlin {
    jvmToolchain(21)
}

// Create task for each day
(1..12).forEach { day ->
    val padded = day.toString().padStart(2, '0')
    tasks.register<JavaExec>("day$padded") {
        group = "aoc"
        mainClass.set("aoc.days.Day${padded}Kt")
        classpath = sourceSets["main"].runtimeClasspath
    }
}

application {
    mainClass.set("aoc.days.Day01Kt")
}

// Adventure game task
tasks.register<JavaExec>("adventure") {
    group = "aoc"
    description = "Launch North Pole Adventure TUI game"
    mainClass.set("aoc.adventure.AdventureGameKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

