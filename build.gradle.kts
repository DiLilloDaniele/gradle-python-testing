plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.5.31"
    java
    alias(libs.plugins.dokka)
    id("com.gradle.plugin-publish") version "1.0.0"
    id("org.danilopianini.gradle-kotlin-qa") version "0.27.0"
    `maven-publish`
    signing
}

group = "org.danieledilillo"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation(gradleApi())
    implementation(gradleKotlinDsl())

    testImplementation(gradleTestKit())
    testImplementation(libs.bundles.kotlin.testing)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("") { // One entry per plugin
            project.logger.warn("${project.group} - ${project.name}")
            id = "${project.group}.${project.name}"
            displayName = "Python Testing Plugin"
            description = "Plugin developed aiming at the automation " +
                "of the testing process in a Python project. This plugin includes " +
                "the possibility to specify the src and test folders of the project " +
                "and perform tests and coverage using all the Python libraries (unittest and coverage modules)." +
                " It is also supported to use Phython virtual environments."
            implementationClass = "io.github.dilillodaniele.gradle.testpy.PyTest"
        }
    }
}

pluginBundle { // These settings are set for the whole plugin bundle
    website = ""
    vcsUrl = ""
    tags = listOf("python", "test", "coverage", "buildpython")
}

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

val testWithJVM18 by tasks.registering(Test::class) { // Also works with JavaExec
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(18))
        }
    )
}
// You can pick JVM's not yet supported by Gradle!
tasks.check {
    dependsOn(testWithJVM18)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = true
    }
}

val javadocJar by tasks.registering(Jar::class) {
    from(tasks.dokkaJavadoc.get().outputDirectory)
    archiveClassifier.set("javadoc")
}

val sourceJar by tasks.registering(Jar::class) {
    from(sourceSets.named("main").get().allSource)
    archiveClassifier.set("sources")
}
/*
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
            val pyTest by creating(MavenPublication::class) {
                from(components["java"])
                // If the gradle-publish-plugins plugin is applied, these are pre-configured
                // artifact(javadocJar)
                // artifact(sourceJar)
                pom {
                    name.set("Greetings plugin")
                    description.set("A test plugin")
                    url.set("???")
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
                        url.set("git@github.com:DanySK/lss-deleted-soon.git")
                        connection.set("git@github.com:DanySK/lss-deleted-soon.git")
                    }
                }
            }
            signing { sign(pyTest) }
        }
    }
}
*/
/*
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
*/