import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation

// Template by Nxllpointer, thanks ❤
// https://github.com/Nxllpointer/AdventOfCode2022/blob/9beb193f826f9110ebe436645f3176bf6da5869a/build.gradle.kts

plugins {
    kotlin("jvm") version "1.7.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

val currentDay = 15

repositories {
    mavenCentral()
}

kotlin {
    target {
        compilations {
            val common by creating

            for (day in 1..currentDay) {
                val dayCompilation = create("day_$day") {
                    associateWith(common)
                    generateAOCBaseStructure(day)

                    dependencies {
                        implementation("io.github.zabuzard.maglev:maglev:1.2")
                    }
                }

                tasks.create<JavaExec>("runDay$day") {
                    group = "aoc"
                    mainClass.set("Day${day}Kt")
                    classpath = dayCompilation.runtimeDependencyFiles
                }
            }

            all {
                kotlinOptions {
                    jvmTarget = "17"
                }
            }
        }
    }

    jvmToolchain(17)
}

fun KotlinCompilation<*>.generateAOCBaseStructure(day: Int) {
    val kotlinDir = defaultSourceSet.kotlin.sourceDirectories.first { it.name == "kotlin" }
    val resourcesDir = defaultSourceSet.resources.srcDirs.first()
    val mainFile = kotlinDir.resolve("Day${day}.kt")
    val inputFile = resourcesDir.resolve("input.txt")
    kotlinDir.mkdirs()
    resourcesDir.mkdirs()
    inputFile.createNewFile()

    if (mainFile.createNewFile()) {
        val templateContent = """
            // AOC Day $day
            fun main() {
                val lines = {}::class.java.getResourceAsStream("input.txt")!!.bufferedReader().readLines()
                
                
            }
        """.trimIndent()

        mainFile.writeText(templateContent)
    }
}
