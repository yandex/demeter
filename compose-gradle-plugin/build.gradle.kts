import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import extensions.configurePom
import extensions.configureRepository
import extensions.plugin

plugins {
    alias(libs.plugins.module.gradle.plugin)
    `kotlin-dsl`
    alias(libs.plugins.vanniktech.maven.publish)
}

plugin(
    name = "demeter-compose",
    pluginId = "com.yandex.demeter.compose",
    implementation = "com.yandex.demeter.compose.plugin.DemeterComposePlugin",
)

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()
    configure(
        GradlePlugin(
            javadocJar = JavadocJar.Javadoc(),
            sourcesJar = true,
        )
    )

    publishing { configureRepository() }
    pom { configurePom("Demeter Compose Gradle Plugin") }
}

dependencies {
    api(projects.gradlePluginUtils)

    api(libs.kotlin.gradlePluginApi)
}
