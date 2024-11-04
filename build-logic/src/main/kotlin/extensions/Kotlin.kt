package extensions

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

internal fun Project.kotlin(configure: Action<KotlinAndroidProjectExtension>): Unit =
    extensions.configure("kotlin", configure)

internal fun Project.java(configure: Action<JavaPluginExtension>): Unit =
    extensions.configure("java", configure)

internal fun Project.optInInternalDemeterApi() {
    tasks.withType<KotlinCompilationTask<*>> {
        compilerOptions.freeCompilerArgs.add("-opt-in=com.yandex.demeter.annotations.InternalDemeterApi")
    }
}
