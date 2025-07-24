import extensions.configurePom
import extensions.configureRepository

plugins {
    alias(libs.plugins.module.jvm.base)
    alias(libs.plugins.vanniktech.maven.publish)
}

mavenPublishing {
    publishToMavenCentral(false)
    signAllPublications()

    publishing { configureRepository() }
    pom { configurePom("Demeter Core") }
}
