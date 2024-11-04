package extensions

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal fun Project.androidApp(configure: Action<ApplicationExtension>): Unit =
    extensions.configure("android", configure)

internal fun Project.androidLib(configure: Action<LibraryExtension>): Unit =
    extensions.configure("android", configure)

internal fun Project.composeCompiler(configure: Action<ComposeCompilerGradlePluginExtension>): Unit =
    extensions.configure("composeCompiler", configure)
