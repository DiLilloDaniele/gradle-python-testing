package io.github.dilillodaniele.gradle.testpy

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigDecimal
import java.util.*

/**
 * Plugin class that performs tests on a Python project.
 */
open class PyTest : Plugin<Project> {

    /**
     * Constructor method for the open class.
     */
    override fun apply(target: Project) {
        with(target) {

            val extension = project.createExtension<PyTestPluginExtension>("pytest", target)
            val pythonFolder = { "$projectDir/${extension.virtualEnvFolder.get()}/$osFolder" }
            val projectPath = target.projectDir.toString().replace("\\", "/")

            tasks.register<Exec>("detailedTest") {
                val path = projectPath + "/" + extension.testSrc.get()
                val files = File(path).walk().filter { it.extension == "py" }
                    .filter { !it.name.contains("__init__") }.map { it.name }
                println(files.toList().toString())

                var args = if (files.toList().size > 1)
                    files.map { "${extension.testSrc.get()}/$it" }.joinToString(separator = " ")
                else
                    files.map { "${extension.testSrc.get()}/$it" }.toList().get(0)
                var command: String = if (extension.useVirtualEnv.get())
                    "${pythonFolder()}/python -m unittest -v $args"
                else
                    "python -m unittest -v $args"

                commandLine(command.split(" ").toList())
                standardOutput = ByteArrayOutputStream()
                doLast {
                    val result = standardOutput.toString()
                    project.logger.warn(result)
                }
            }

            tasks.register<Exec>("installCoverageGlobally") {
                commandLine("pip", "list")
                standardOutput = ByteArrayOutputStream()
                doLast {
                    val result = standardOutput.toString()
                    val installed = result.contains("coverage")
                    project.logger.warn("coverage is installed: $installed")
                    if (!installed && !extension.useVirtualEnv.get()) {
                        val output = project.runCommand("python", "-m", "pip", "install", "coverage")
                        project.logger.warn("$output")
                        project.logger.warn("----------------------")
                        project.logger.warn("Coverage installed correctly")
                    }
                }
            }

            tasks.register<Exec>("installCoverageOnVenv") {
                commandLine("${pythonFolder()}/pip", "list")
                standardOutput = ByteArrayOutputStream()
                doLast {
                    val result = standardOutput.toString()
                    val installed = result.contains("coverage")
                    project.logger.warn("coverage is installed: $installed")
                    if (!installed) {
                        val output = project.runCommand("${pythonFolder()}/python", "-m", "pip", "install", "coverage")
                        project.logger.warn("$output")
                        project.logger.warn("----------------------")
                        project.logger.warn("Coverage installed correctly")
                    }
                }
            }

            tasks.register<Task>("installCoverage") {
                if (!extension.useVirtualEnv.get())
                    dependsOn("installCoverageGlobally")
                else
                    dependsOn("installCoverageOnVenv")
            }

            tasks.register<Exec>("doCoverage") {
                dependsOn("installCoverage")
                project.logger.debug("Executing coverage")
                val path = extension.testSrc.get().replace("/", ".")
                project.logger.debug("Project path: $path")
                val command = if (extension.useVirtualEnv.get())
                    "${pythonFolder()}/coverage run -m unittest discover -s $path"
                else
                    "python -m coverage run -m unittest discover -s $path"
                commandLine(command.split(" ").toList())
                standardOutput = ByteArrayOutputStream()
                doLast {
                    val result = standardOutput.toString()
                    project.logger.warn("the result value is: $result")
                }
            }

            tasks.register<Exec>("checkCoverage") {
                dependsOn("doCoverage")
                val command = if (extension.useVirtualEnv.get())
                    "${pythonFolder()}/python -m coverage json --omit=src/test/*"
                else
                    "python -m coverage json --omit=src/test/*"
                commandLine(command.split(" ").toList())
                doLast {
                    val file = File("$projectPath/coverage.json")
                    val json = groovy.json.JsonSlurper().parseText(file.readText()) as Map<*, *>
                    val totals: Map<*, *>? = json["totals"] as Map<*, *>?
                    val value = totals?.get("percent_covered") as BigDecimal
                    if (value <= BigDecimal(extension.minCoveragePercValue.get()))
                        error("Coverage percentage is lower than the threshold")
                    else
                        project.logger.warn(COV_OK)
                }
            }

            tasks.register<Exec>("performTests") {
                dependsOn("checkCoverage")
                val srcPackage = extension.testSrc.get().replace("/", ".")
                val command = if (extension.useVirtualEnv.get())
                    "${pythonFolder()}/python -m unittest discover -s $srcPackage"
                else
                    "python -m unittest discover -s $srcPackage"
                commandLine(command.split(" ").toList())
                standardOutput = ByteArrayOutputStream()
                doLast {
                    val result = standardOutput.toString()
                    project.logger.warn("$result")
                }
            }
        }
    }

    companion object {

        /**
         * Coverage output value.
         */
        const val COV_OK = "coverage percentage ok"

        enum class OS {
            WINDOWS, LINUX, MAC
        }

        /**
         * Utility method that return the OS type of the executing machine.
         */
        fun getOS(): OS? {
            val os = System.getProperty("os.name").lowercase(Locale.getDefault())
            return when {
                os.contains("win") -> {
                    OS.WINDOWS
                }
                os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
                    OS.LINUX
                }
                os.contains("mac") -> {
                    OS.MAC
                }
                else -> null
            }
        }

        /**
         * Utility method that returns the OS specific folder to create
         * for a virtual environment.
         */
        val osFolder = when (getOS()) {
            OS.WINDOWS -> "Scripts"
            OS.LINUX -> "bin"
            OS.MAC -> "bin"
            else -> {
                error("Unknown Operating System")
            }
        }

        private inline fun <reified T> Project.createExtension(name: String, vararg args: Any?): T =
            project.extensions.create(name, T::class.java, *args)

        private fun Project.runCommand(vararg cmd: String) = projectDir.runCommandInFolder(*cmd)

        private fun File.runCommandInFolder(vararg cmd: String) = Runtime.getRuntime()
            .exec(cmd, emptyArray(), this)
            .inputStream
            .bufferedReader()
            .readText()
            .trim()
            .takeIf { it.isNotEmpty() }
    }
}
