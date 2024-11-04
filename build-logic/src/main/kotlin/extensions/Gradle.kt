package extensions

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.support.delegates.DependencyHandlerDelegate
import org.gradle.kotlin.dsl.the

internal val Project.libs
    get(): LibrariesForLibs = the<LibrariesForLibs>()

internal fun DependencyHandlerDelegate.implementation(
    provider: Provider<MinimalExternalModuleDependency>,
) {
    add("implementation", provider.get())
}

internal fun DependencyHandlerDelegate.testImplementation(
    provider: Provider<MinimalExternalModuleDependency>,
) {
    add("testImplementation", provider.get())
}
