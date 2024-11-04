package extensions

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

fun Project.includeTests() {
    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        }
    }

    dependencies {
        testImplementation(libs.kotlin.test)
        testImplementation(libs.coroutinesTest)
        testImplementation(libs.test.rules)
        testImplementation(libs.test.runner)
        testImplementation(libs.junit.jupiter)
    }
}
