package com.yandex.demeter.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.GradleException
import org.gradle.api.Project

inline val ClassData.isKtIntrinsics: Boolean
    get() = className.endsWith("WhenMappings")

val Project.android
    get() = extensions.getByType(ApplicationExtension::class.java)

fun Project.androidComponents(block: ApplicationAndroidComponentsExtension.() -> Unit) {
    extensions.getByType(ApplicationAndroidComponentsExtension::class.java).block()
}

fun Project.requireAndroidApp() {
    if (!plugins.hasPlugin(AppPlugin::class.java)) {
        throw GradleException("Demeter plugin must be applied only to 'com.android.application' modules. " +
            "Current module '${project.name}' does not have the Android Application plugin applied.")
    }
}

fun Project.validateDependencies() {
    val validator = DependencyValidator(this)
    val issues = validator.validateProjectDependencies()

    if (issues.isNotEmpty()) {
        validator.logIssues(issues)

        val criticalIssues = issues.filter { it.type == IssueType.VERSION_CATALOG_ACCESSOR }
        if (criticalIssues.isNotEmpty()) {
            logger.warn("Demeter detected ${criticalIssues.size} critical dependency issue(s) that may cause build failures.")
            logger.warn("Please review the suggestions above to fix these issues.")
        }
    }
}

private fun createEnhancedGradleException(message: String, cause: Throwable): GradleException {
    val enhancedMessage = buildString {
        appendLine("Demeter Plugin Error: $message")

        when {
            cause.message?.contains("No variants") == true -> {
                appendLine("This appears to be a dependency variant resolution issue.")
                appendLine("Common causes:")
                appendLine("1. Incorrect Version Catalog usage (e.g., using library accessor instead of version reference)")
                appendLine("2. Conflicting dependency variants or artifact types")
                appendLine("3. Missing or incorrect dependency configuration")
                appendLine("Please check your dependency declarations in build.gradle files.")
            }
            cause.message?.contains("artifactType") == true -> {
                appendLine("This is an artifact type compatibility issue.")
                appendLine("The dependency may be declared with an incorrect version reference.")
                appendLine("Check if you're using Version Catalog accessors correctly.")
            }
            cause.message?.contains("Accessor") == true -> {
                appendLine("This appears to be a Version Catalog accessor misuse.")
                appendLine("Make sure you're using version references (libs.versions.x) not library accessors (libs.x) in version strings.")
            }
            else -> {
                appendLine("Original error: ${cause.message}")
            }
        }
    }

    return GradleException(enhancedMessage, cause)
}
