plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.detekt).apply(true)
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        toolVersion = rootProject.libs.versions.detekt.get()
        autoCorrect = true
        parallel = true
        config.setFrom("$rootDir/detekt/detekt-rules.yaml")
        source.setFrom(
            "src/main/java",
            "src/test/java",
            "src/androidTest/java",
            "src/main/kotlin",
            "src/test/kotlin",
            "src/androidTest/kotlin",
        )
    }

    dependencies {
        detektPlugins(rootProject.libs.detekt.cli)
        detektPlugins(rootProject.libs.detekt.formatting)
    }
}

task("publish") {
    dependsOn(gradle.includedBuild("demeter-gradle-plugin").task(":publish"))
}
