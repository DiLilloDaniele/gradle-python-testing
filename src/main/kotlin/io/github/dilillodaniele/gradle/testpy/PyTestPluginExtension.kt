package io.github.dilillodaniele.gradle.testpy

import org.gradle.api.Project
import org.gradle.api.provider.Property

/**
 * Default coverage percentage acceptable.
 */
const val DEFAULT_COVERAGE_PERC = 50

/**
 * Python Test Plugin [project] extension.
 * if [useVirtualEnv] true, the [virtualEnvFolder] is evaluated: if bin-lib ir Script-Lib
 * exists, try finding covegare bin and use it.
 * Otherwise if [coverageAutoInstall] true install it globally, else if false throw error.
 * The [testSrc] refers to the folder that contains all the python tests.
 * Finally, the [minCoveragePercValue] refers to the minimum percentage of coverage acceptable.
 */
open class PyTestPluginExtension @JvmOverloads constructor(
    private val project: Project,
    val testSrc: Property<String> = project.propertyWithDefault("src/test"),
    val coverageAutoInstall: Property<Boolean> = project.propertyWithDefault(true),
    val virtualEnvFolder: Property<String> = project.propertyWithDefault(project.path),
    val minCoveragePercValue: Property<Int> = project.propertyWithDefault(DEFAULT_COVERAGE_PERC),
    val useVirtualEnv: Property<Boolean> = project.propertyWithDefault(false)
) {
    companion object {
        /**
         * Extension name.
         */
        const val EXTENSION_NAME = "pytest_plugin"

        /**
         * New extension method for Project class that assign a default value
         * to a generic type variable.
         */
        private inline fun <reified T> Project.propertyWithDefault(default: T): Property<T> =
            objects.property(T::class.java).apply { convention(default) }
    }
}
