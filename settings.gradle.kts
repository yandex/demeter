enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

apply(from = "build-logic/src/main/kotlin/scripts/base-settings.gradle.kts")
val configureRepositories: Settings.() -> Unit by extra
configureRepositories()

pluginManagement {
    includeBuild("build-logic")

    includeBuild("demeter-gradle-plugin")
}

rootProject.name = "demeter"

include(
    ":showcase",
    ":demeter-core",
    ":demeter-profiler",
    ":demeter-profiler-base",
    ":demeter-profiler-ui",
    ":demeter-tracer-profiler-plugin",
    ":demeter-inject-profiler-plugin",
    ":demeter-compose-profiler-plugin",
    ":demeter-compose-compiler-plugin",
    ":demeter-flipper",
    ":demeter-benchmark",
)
