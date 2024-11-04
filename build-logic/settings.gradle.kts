apply(from = "src/main/kotlin/scripts/base-settings.gradle.kts")
val configureRepositories: Settings.() -> Unit by extra
configureRepositories()

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
