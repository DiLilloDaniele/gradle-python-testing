package io.github.dilillodaniele.gradle.testpy

import io.kotest.core.spec.style.StringSpec
import org.apache.commons.io.FileUtils
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit

class PluginTests : StringSpec(
    {
        "the plugin task that performs detailed tests should work correctly" {
            val project = tempFolderVenv
            val result = project.runGradle("detailedTest", "--stacktrace")
            println(result)
            assert(result.contains("OK"))
        }
        "the plugin should correctly install coverage globally if venv's not used" {
            val project = tempFolderNoVenv
            val result = project.runGradle("installCoverageGlobally", "--stacktrace")
            println(result)
            assert(result.contains("installed"))
        }
        "the plugin should correctly create the coverage file" {
            val project = tempFolderNoVenv
            project.runGradle("doCoverage", "--stacktrace", "--debug")
            assert(Files.exists(File("${project.root}/.coverage").toPath()))
        }
        "the check coverage task should work correctly" {
            val project = tempFolderVenv
            println(project.root)
            val result = project.runGradle("checkCoverage", "--stacktrace")
            assert(result.contains(PyTest.COV_OK))
        }
        "the plugin task that performs tests in a general manner" {
            val project = tempFolderVenv
            val result = project.runGradle("performTests", "--stacktrace")
            println(result)
            assert(result.contains("OK"))
        }
    }
) {
    companion object {
        const val virtualEnvFolder: String = "env"

        val tempFolderVenv = configuredPlugin(
            """
            useVirtualEnv.set(true)
            virtualEnvFolder.set("$virtualEnvFolder")
            """.trimIndent()
        )
            .virtualEnvStartup()
            .moveFolder(File(PluginTests::class.java.getResource("/python").toURI()))

        val tempFolderNoVenv = configuredPlugin("useVirtualEnv.set(false)")
            .moveFolder(File(PluginTests::class.java.getResource("/python").toURI()))

        fun folder(closure: TemporaryFolder.() -> Unit) = TemporaryFolder().apply {
            create()
            closure()
        }

        fun TemporaryFolder.file(name: String, content: () -> String) = newFile(name).writeText(content().trimIndent())

        fun TemporaryFolder.runCommand(vararg command: String, wait: Long = 30) {
            val process = ProcessBuilder(*command)
                .directory(root)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start()
            process.waitFor(wait, TimeUnit.SECONDS)
            try {
                require(process.exitValue() == 0) {
                    "command '${command.joinToString(" ")}' failed with exit value ${process.exitValue()}"
                }
            } catch (ex: IllegalThreadStateException) {
                println("$command")
                println("$ex - ${ex.message}")
            }
        }

        fun TemporaryFolder.runCommand(command: String, wait: Long = 30) = runCommand(
            *command.split(" ").toTypedArray(),
            wait = wait,
        )

        fun TemporaryFolder.moveFolder(src: File): TemporaryFolder {
            FileUtils.copyDirectory(src, File(this.root.path + "/src"))
            return this
        }

        fun TemporaryFolder.virtualEnvStartup(): TemporaryFolder {
            runCommand("python -m venv $virtualEnvFolder")
            runCommand("mkdir src")
            return this
        }

        fun TemporaryFolder.runGradle(
            vararg arguments: String = arrayOf(),
        ): String = GradleRunner
            .create()
            .withProjectDir(root)
            .withPluginClasspath()
            .withArguments(*arguments)
            .build().output

        fun configuredPlugin(
            pluginConfiguration: String = "",
            otherChecks: TemporaryFolder.() -> Unit = {},
        ): TemporaryFolder = folder {
            file("settings.gradle") { "rootProject.name = 'testproject'" }
            file("build.gradle.kts") {
                """
                
                plugins {
                    id("io.github.dilillodaniele.pytest")
                }
               
                pytest {
                    testSrc.set("src/test")
                    $pluginConfiguration
                }
               
                """.trimIndent()
            }
            otherChecks()
        }
    }
}
