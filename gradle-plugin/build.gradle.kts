import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import extensions.configurePom
import extensions.configureRepository
import extensions.plugin

plugins {
    alias(libs.plugins.module.jvm.base).apply(false)
    alias(libs.plugins.module.gradle.plugin)
    `kotlin-dsl`
    alias(libs.plugins.vanniktech.maven.publish)
}

plugin(
    name = "demeter",
    pluginId = "com.yandex.demeter",
    implementation = "com.yandex.demeter.plugin.DemeterPlugin",
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
    pom { configurePom("Demeter Gradle Plugin") }
}

dependencies {
    api(projects.tracerGradlePlugin)
    api(projects.injectGradlePlugin)
    api(projects.composeGradlePlugin)

    implementation(libs.tools.gradle)
}
