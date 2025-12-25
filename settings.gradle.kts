enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

apply(from = "build-logic/src/main/kotlin/scripts/base-settings.gradle.kts")
val configureRepositories: Settings.() -> Unit by extra
configureRepositories()

pluginManagement {
    includeBuild("build-logic")

    includeBuild("gradle-plugin")
}

rootProject.name = "demeter"

include(
    ":showcase",
    ":core",
    ":profiler",
    ":profiler-base",
    ":profiler-ui",
    ":profiler-tracer-plugin",
    ":profiler-tracer-ui-plugin",
    ":profiler-inject-plugin",
    ":profiler-inject-ui-plugin",
    ":profiler-compose-plugin",
    ":profiler-compose-ui-plugin",
    ":compose-compiler-plugin",
    ":flipper",
    ":benchmark",
)
