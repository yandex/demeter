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
    name = "demeter-inject",
    pluginId = "com.yandex.demeter.inject",
    implementation = "com.yandex.demeter.inject.plugin.DemeterInjectPlugin",
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
    pom { configurePom("Demeter Inject Gradle Plugin") }
}

dependencies {
    api(projects.gradlePluginUtils)

    implementation(libs.tools.gradleApi)
    implementation(libs.asm.commons)
    implementation(libs.asm.utils)
}
