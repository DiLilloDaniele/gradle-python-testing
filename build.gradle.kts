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
            description = "Plugin that allow to automate Python testing on a project"
            implementationClass = "PyTestPlugin"
        }
    }
}

pluginBundle { // These settings are set for the whole plugin bundle
    website = ""
    vcsUrl = ""
    tags = listOf("python", "test", "coverage", "buildpython")
}

