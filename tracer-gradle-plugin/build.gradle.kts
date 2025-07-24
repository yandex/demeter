import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import extensions.configurePom
import extensions.configureRepository
import extensions.includeTests
import extensions.plugin

plugins {
    alias(libs.plugins.module.gradle.plugin)
    `kotlin-dsl`
    alias(libs.plugins.vanniktech.maven.publish)
}

plugin(
    name = "demeter-tracer",
    pluginId = "com.yandex.demeter.tracer",
    implementation = "com.yandex.demeter.tracer.plugin.DemeterTracerPlugin",
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
    pom { configurePom("Demeter Tracer Gradle Plugin") }
}

includeTests()

dependencies {
    api(projects.gradlePluginUtils)

    implementation(libs.tools.gradle)
    implementation(libs.tools.gradleApi)
    implementation(libs.asm.commons)
    implementation(libs.asm.utils)

    testImplementation(gradleTestKit())
}
