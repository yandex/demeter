apply(from = "../build-logic/src/main/kotlin/scripts/base-settings.gradle.kts")
val configureRepositories: Settings.() -> Unit by extra
configureRepositories()

pluginManagement {
    includeBuild("../build-logic")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

include(":gradle-plugin-utils")
project(":gradle-plugin-utils").projectDir = file("../gradle-plugin-utils")
include(":tracer-gradle-plugin")
project(":tracer-gradle-plugin").projectDir = file("../tracer-gradle-plugin")
include(":inject-gradle-plugin")
project(":inject-gradle-plugin").projectDir = file("../inject-gradle-plugin")
include(":compose-gradle-plugin")
project(":compose-gradle-plugin").projectDir = file("../compose-gradle-plugin")

rootProject.name = "gradle-plugin"
