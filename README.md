# gradle-python-testing
Gradle Plugin that simplify the testing process of a Python project.
```bash
id("io.github.DiLilloDaniele.gradle-python-testing") version "1.4.1"
```
To set the plugin add the following Kotlin code example into your build script, 
and configure it correctly.
```kotlin
pytest {
    testSrc.set("src/test/python") // test folder that contains all python tests
    minCoveragePercValue.set(80) // min 0 - max 100 acceptable percentage of coverage
    useVirtualEnv.set(true) // true if you use a virtual environment or global libraries
    virtualEnvFolder.set(".gradle/python") // virtual env folder if you use it
    coverageAutoInstall.set(true) // if coverage module is not installed, install it
}
```
To work correctly, the plugin needs that all the python test files are located 
into the <i> testSrc </i> folder (subfolders currently not supported).
Furthermore, all test files must be called with the test prefix (testXXX.xx) and 
they must be defined with methods whose names start with the letters test, because of the 
<i> unittest </i> component, used to perform tests.

As mentioned into the <a href='https://docs.python.org/3/library/unittest.html'> Python unittest documentation </a>, an example of
a well defined test to use with this plugin is the following:
```python
import unittest

class TestStringMethods(unittest.TestCase):

    def test_upper(self):
        self.assertEqual('foo'.upper(), 'FOO')

    def test_isupper(self):
        self.assertTrue('FOO'.isupper())
        self.assertFalse('Foo'.isupper())

    def test_split(self):
        s = 'hello world'
        self.assertEqual(s.split(), ['hello', 'world'])
        # check that s.split fails when the separator is not a string
        with self.assertRaises(TypeError):
            s.split(2)

if __name__ == '__main__':
    unittest.main()
```

In order to use this plugin aiming at automate the test phase in the automatic build process, you can
add the following dependency into your Gradle project (example written in Kotlin):
```kotlin
tasks.named("test") {
    dependsOn("performTests")
}
```