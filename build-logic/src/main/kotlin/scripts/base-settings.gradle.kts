package scripts

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.extra

val configureRepositories: Settings.() -> Unit by extra {
    return@extra {
        pluginManagement {
            repositories.repositories()
        }
        dependencyResolutionManagement {
            repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
            repositories.repositories()
        }
    }
}

private fun RepositoryHandler.repositories() {
    google()
    mavenCentral()
    maven("https://groovy.jfrog.io/artifactory/plugins-release/") {
        content {
            // https://github.com/reddit/IndicatorFastScroll/issues/45
            includeGroup("com.reddit")
        }
    }
    gradlePluginPortal()
}
