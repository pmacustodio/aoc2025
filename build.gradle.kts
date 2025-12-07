plugins {
    kotlin("jvm") version "1.9.22"
    application
}

repositories {
    mavenCentral()
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

