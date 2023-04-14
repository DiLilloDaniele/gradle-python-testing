package io.github.dilillodaniele.gradle.testpy

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

open class PyTest : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            val extension = project.createExtension<PyTestPluginExtension>("pytest", target)

            tasks.register<Exec>("detailedTest") {
                val projectPath = target.projectDir.toString().replace("\\", "/")
                val path = projectPath + "/" + extension.testSrc.get()
                val files = File(path).walk().filter { it.extension == "py" }
                    .filter { !it.name.contains("__init__") }.map { it.name }
                println(files.toList().toString())

                var args = if(files.toList().size > 1)
                    files.map { "${extension.testSrc.get()}/${it}" }.joinToString(separator = " ")
                else
                    files.map { "${extension.testSrc.get()}/${it}" }.toList().get(0)
                var command: String = if (extension.useVirtualEnv.get())
                    "${projectDir}/${extension.virtualEnvFolder.get()}/$osFolder/python -m unittest -v " + args
                else
                    "python -m unittest -v $args"

                commandLine(command.split(" ").toList())
                standardOutput = ByteArrayOutputStream()
                doLast {
                    val result = standardOutput.toString()
                    project.logger.warn(result)
                }
            }

        }
    }

    companion object {

        enum class OS {
            WINDOWS, LINUX, MAC
        }

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

        val osFolder = when (getOS())
        {
            OS.WINDOWS -> "Scripts"
            OS.LINUX -> "bin"
            OS.MAC -> "bin"
            else -> {
                println("Unknown Operating System")
                throw IllegalStateException()
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
