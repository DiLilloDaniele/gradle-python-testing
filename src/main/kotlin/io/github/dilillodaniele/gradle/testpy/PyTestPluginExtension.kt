package io.github.dilillodaniele.gradle.testpy

import org.gradle.api.Project
import org.gradle.api.provider.Property

/**
 * Python Test Plugin [project] extension.
 * if [useVirtualEnv] true, the [virtualEnvFolder] is evaluated: if bin-lib ir Script-Lib
 * exists, try finding covegare bin and use it.
 * Otherwise if [coverageAutoInstall] true install it globally, else if false throw error
 */
open class PyTestPluginExtension  @JvmOverloads constructor(
    private val project: Project,
    val testSrc: Property<String> = project.propertyWithDefault("src"),
    val coverageAutoInstall: Property<Boolean> = project.propertyWithDefault(true),
    val virtualEnvFolder: Property<String> = project.propertyWithDefault(project.path),
    val minCoveragePercValue: Property<Int> = project.propertyWithDefault(50),
    val useVirtualEnv: Property<Boolean> = project.propertyWithDefault(false)
) {

    companion object {

        const val EXTENSION_NAME = "pytest_plugin"

        private inline fun <reified T> Project.propertyWithDefault(default: T): Property<T> =
            objects.property(T::class.java).apply { convention(default) }
    }

}
