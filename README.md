# gradle-python-testing
Gradle Plugin that simplify the testing process of a Python project
```bash
id("io.github.DiLilloDaniele.gradle-python-testing") version "1.4.1"
```
To set the plugin use the following Kotlin code example into your build script.
```kotlin
pytest {
    testSrc.set("src/test/python")
    minCoveragePercValue.set(80)
    useVirtualEnv.set(true)
    virtualEnvFolder.set(".gradle/python")
}
```
To work correctly, the plugin needs that all the python test files are located 
into the <i> testSrc </i> folder (subfolders currently not supported).
