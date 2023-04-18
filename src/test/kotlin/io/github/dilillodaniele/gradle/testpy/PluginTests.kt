package io.github.dilillodaniele.gradle.testpy

import io.kotest.core.spec.style.StringSpec
import org.apache.commons.io.FileUtils
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.util.concurrent.TimeUnit

class PluginTests : StringSpec(
    {
        "the plugin task that performs detailed tests should work correctly" {
            val project = configuredPlugin()
            val file = File(PluginTests::class.java.getResource("/python").toURI())
            project.virtualEnvStartup()
            project.moveFolder(file)
            val result = project.runGradle("detailedTest", "--stacktrace")
            println(result)
            assert(result.contains("OK"))
        }
    }
) {
    companion object {
        const val virtualEnvFolder: String = "env"

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

        fun TemporaryFolder.runCommand(command: String, wait: Long = 10) = runCommand(
            *command.split(" ").toTypedArray(),
            wait = wait,
        )

        fun TemporaryFolder.moveFolder(src: File) {
            FileUtils.copyDirectory(src, File(this.root.path + "/src"))
        }

        fun TemporaryFolder.virtualEnvStartup() {
            runCommand("python -m venv $virtualEnvFolder")
            runCommand("mkdir src")
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
            // pluginConfiguration: String = "",
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
                    useVirtualEnv.set(true)
                    virtualEnvFolder.set("$virtualEnvFolder")
                }
               
                """.trimIndent()
            }
            otherChecks()
        }
    }
}
