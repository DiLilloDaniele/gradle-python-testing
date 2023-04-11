package io.github.dilillodaniele.gradle.testpy

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import java.io.ByteArrayOutputStream
import java.io.File

open class PyTest : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = project.createExtension<PyTestPluginExtension>("pytest", target)

            tasks.register<Exec>("detailedTest") {
                val files = File(extension.testSrc.get()).walk().filter { it.extension == "py" }
                    .filter { it.name != "__init__" }.map { it.path }
                println(files.toList().toString())
                project.logger.warn("----------------TESTS-----------------")
                var args = ""
                files.toList().forEach {
                    args += it
                    args += " "
                }
                args = args.replace("\\", "/")
                project.logger.warn(args)
                val command = if (extension.useVirtualEnv.get())
                    "${extension.virtualEnvFolder.get()}/python -m unittest -v " + args
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
