package com.yandex.demeter.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.logging.Logger

class DependencyValidator(private val project: Project) {
    
    private val logger: Logger = project.logger
    
    fun validateProjectDependencies(): List<DependencyIssue> {
        val issues = mutableListOf<DependencyIssue>()
        
        project.configurations.forEach { configuration ->
            if (configuration.isCanBeResolved) {
                try {
                    validateConfiguration(configuration, issues)
                } catch (e: Exception) {
                    logger.debug("Failed to validate configuration ${configuration.name}: ${e.message}")
                }
            }
        }
        
        return issues
    }
    
    private fun validateConfiguration(configuration: Configuration, issues: MutableList<DependencyIssue>) {
        configuration.allDependencies.forEach { dependency ->
            validateDependency(dependency, configuration.name, issues)
        }
    }
    
    private fun validateDependency(dependency: Dependency, configurationName: String, issues: MutableList<DependencyIssue>) {
        val version = dependency.version ?: return
        
        if (isVersionCatalogAccessor(version)) {
            issues.add(
                DependencyIssue(
                    type = IssueType.VERSION_CATALOG_ACCESSOR,
                    dependency = "${dependency.group}:${dependency.name}:$version",
                    configuration = configurationName,
                    suggestion = generateVersionCatalogSuggestion(dependency, version)
                )
            )
        }
        
        if (hasArtifactTypeIssue(version)) {
            issues.add(
                DependencyIssue(
                    type = IssueType.ARTIFACT_TYPE_MISMATCH,
                    dependency = "${dependency.group}:${dependency.name}:$version",
                    configuration = configurationName,
                    suggestion = "This dependency appears to have an artifact type compatibility issue. Check your dependency declaration syntax."
                )
            )
        }
    }
    
    private fun isVersionCatalogAccessor(version: String): Boolean {
        return version.contains("LibrariesForLibs$") || 
               version.contains("VersionAccessors") ||
               version.contains("LibraryAccessors") ||
               version.matches(Regex(".*\\$.*Accessors.*"))
    }
    
    private fun hasArtifactTypeIssue(version: String): Boolean {
        return version.matches(Regex(".*[0-9a-f]{8}.*")) && version.contains("Accessor")
    }
    
    private fun generateVersionCatalogSuggestion(dependency: Dependency, currentVersion: String): String {
        val baseName = extractBaseName(currentVersion)
        return buildString {
            appendLine("Version Catalog accessor detected instead of version string.")
            appendLine("Current: implementation(\"${dependency.group}:${dependency.name}:\${libs.$baseName}\")")
            appendLine("Suggested fixes:")
            appendLine("  1. Use version reference: implementation(\"${dependency.group}:${dependency.name}:\${libs.versions.$baseName}\")")
            appendLine("  2. Use direct library reference: implementation(libs.$baseName)")
        }
    }
    
    private fun extractBaseName(accessorName: String): String {
        return accessorName
            .substringAfterLast("$")
            .substringBefore("Accessor")
            .lowercase()
            .takeIf { it.isNotEmpty() } ?: "library"
    }
    
    fun logIssues(issues: List<DependencyIssue>) {
        if (issues.isNotEmpty()) {
            logger.warn("Demeter Plugin detected ${issues.size} dependency configuration issue(s):")
            issues.forEach { issue ->
                logger.warn("${issue.type.displayName}: ${issue.dependency} in '${issue.configuration}' configuration")
                logger.warn("Suggestion: ${issue.suggestion}")
                logger.warn("---")
            }
        }
    }
}

data class DependencyIssue(
    val type: IssueType,
    val dependency: String,
    val configuration: String,
    val suggestion: String
)

enum class IssueType(val displayName: String) {
    VERSION_CATALOG_ACCESSOR("Version Catalog Accessor Misuse"),
    ARTIFACT_TYPE_MISMATCH("Artifact Type Compatibility Issue")
}