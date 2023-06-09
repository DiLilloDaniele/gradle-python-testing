plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    java
    `maven-publish`
    signing
    alias(libs.plugins.dokka)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.taskTree)
    alias(libs.plugins.multiJvmTesting)
}

group = "io.github.DiLilloDaniele"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation("commons-io:commons-io:2.11.0")

    testImplementation(gradleTestKit())
    testImplementation(libs.bundles.kotlin.testing)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

gitSemVer {
    maxVersionLength.set(20)
    buildMetadataSeparator.set("-")
}

val infoName = "Python Testing Plugin"
val infoDescription = "Plugin developed aiming at the automation " +
    "of the testing process in a Python project. This plugin includes " +
    "the possibility to specify the src and test folders of the project " +
    "and perform tests and coverage using all the Python libraries (unittest and coverage modules)." +
    " It is also supported to use Phython virtual environments."

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.test {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    )
}

tasks.test {
    testLogging.showStandardStreams = true
}

val testWithJVM18 by tasks.registering(Test::class) { // Also works with JavaExec
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(18))
        }
    )
}

tasks.check {
    dependsOn(testWithJVM18)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = true
    }
}

multiJvm {
    maximumSupportedJvmVersion.set(latestJavaSupportedByGradle)
}

publishing {
    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            // Pass the pwd via -PmavenCentralPwd='yourPassword'
            val mavenCentralPwd: String? by project
            credentials {
                username = "danidilo99"
                password = mavenCentralPwd
            }
        }
        publications {
            val testPython by creating(MavenPublication::class) {
                from(components["java"])
                pom {
                    name.set(infoName)
                    description.set(infoDescription)
                    url.set("https://github.com/DiLilloDaniele/gradle-python-testing")
                    licenses {
                        license {
                            name.set("MIT")
                        }
                    }
                    developers {
                        developer {
                            name.set("Daniele Di Lillo")
                        }
                    }
                    scm {
                        url.set("git@github.com:DiLilloDaniele/gradle-python-testing.git")
                        connection.set("git@github.com:DiLilloDaniele/gradle-python-testing.git")
                    }
                }
            }
        }
    }
}

if (System.getenv("CI") == "true") {
    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
} else {
    signing {
        useGpgCmd()
        sign(configurations.archives.get())
    }
}

gradlePlugin {
    plugins {
        create("PyTest") { // One entry per plugin
            id = "${project.group}.${project.name}"
            displayName = infoName
            description = infoDescription
            implementationClass = "io.github.dilillodaniele.gradle.testpy.PyTest"
        }
    }
}

pluginBundle {
    website = "https://github.com/DiLilloDaniele/gradle-python-testing"
    vcsUrl = "https://github.com/DiLilloDaniele/gradle-python-testing.git"
    tags = listOf("python", "test", "coverage", "buildpython")
}
