package extensions

import BuildConfig
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin

fun Project.publishPlugin() {
    plugins.apply(MavenPublishPlugin::class.java)
    configurePublishSigning()
    javaExtension {
        withSourcesJar()
        withJavadocJar()
    }
    configurePublishingPlugin()
}

fun Project.publishLib(artifactId: String) {
    plugins.apply(MavenPublishPlugin::class.java)
    configurePublishSigning()

    when {
        plugins.hasPlugin(JavaPlugin::class.java) -> {
            javaExtension {
                withSourcesJar()
                withJavadocJar()
            }
            configurePublishingLib(artifactId, components["java"])
        }

        plugins.hasPlugin(AndroidBasePlugin::class.java) -> {
            configureAndroidLib()
            afterEvaluate {
                configurePublishingLib(artifactId, components["release"])
            }
        }
    }
}

private fun Project.configurePublishingPlugin() {
    afterEvaluate {
        tasks.withType<GenerateMavenPom>().configureEach {
            doFirst {
                with (pom) {
                    name.set("Demeter")
                    url.set("https://github.com/yandex/demeter.git")
                    description.set("Performance measurement library")
                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    developers {
                        developer {
                            id.set("Yandex LLC")
                            name.set("Yandex LLC")
                            email.set("android-dev@yandex-team.ru")
                        }
                    }
                    scm {
                        connection.set("scm:git:git@github.com:yandex/demeter.git")
                        developerConnection.set("scm:git:git@github.com:yandex/demeter.git")
                        url.set("git:git@github.com:yandex/demeter.git")
                    }
                }
            }
        }
    }
}

private fun Project.configurePublishingLib(
    artifact: String,
    component: SoftwareComponent,
) {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = BuildConfig.demeterGroup
                artifactId = artifact
                version = BuildConfig.demeterVersion

                generatePom()

                from(component)
            }
        }
    }
    configurePublishRepositories()
}

internal fun Project.configurePublishRepositories() {
    publishing {
        repositories {
            val publishUrl = System.getenv("PUBLISH_URL") ?: return@repositories
            maven(publishUrl) {
                credentials {
                    username = System.getenv("PUBLISH_USERNAME")
                    password = System.getenv("PUBLISH_TOKEN")
                }
            }
        }
    }
}

internal fun Project.configurePublishSigning() {
    val keyId = System.getenv("PUBLISH_SIGNING_KEY_ID") ?: return
    val password = System.getenv("PUBLISH_SIGNING_PASSWORD") ?: return
    val ringFile = System.getenv("PUBLISH_SIGNING_KEY_RING_FILE") ?: return

    plugins.apply(SigningPlugin::class.java)

    project.extra["signing.keyId"] = keyId
    project.extra["signing.password"] = password
    project.extra["signing.secretKeyRingFile"] = ringFile

    publishing {
        signing {
            publications.configureEach {
                sign(this)
            }
        }
    }

    tasks.withType<AbstractPublishToMaven>().configureEach {
        val signingTasks = tasks.withType<Sign>()
        mustRunAfter(signingTasks)
    }

    tasks.withType<PublishToMavenRepository>().configureEach {
        val signingTasks = tasks.withType<Sign>()
        mustRunAfter(signingTasks)
    }
}

internal fun Project.configureAndroidLib() {
    androidLib {
        publishing {
            singleVariant("release") {
                withSourcesJar()
                withJavadocJar()
            }
        }
    }
}

private fun MavenPublication.generatePom() {
    pom {
        name.set("Demeter")
        description.set("Performance measurement library")
        url.set("https://github.com/yandex/demeter.git")
        licenses {
            license {
                name.set("Apache-2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0")
            }
        }
        developers {
            developer {
                id.set("Yandex LLC")
                name.set("Yandex LLC")
                email.set("android-dev@yandex-team.ru")
            }
        }
        scm {
            connection.set("scm:git:git@github.com:yandex/demeter.git")
            developerConnection.set("scm:git:git@github.com:yandex/demeter.git")
            url.set("git:git@github.com:yandex/demeter.git")
        }
    }
}

internal fun Project.publishing(configure: org.gradle.api.Action<PublishingExtension>): Unit =
    extensions.configure("publishing", configure)

internal fun Project.signing(configure: org.gradle.api.Action<SigningExtension>): Unit =
    extensions.configure("signing", configure)

internal fun Project.javaExtension(configure: org.gradle.api.Action<JavaPluginExtension>): Unit =
    extensions.configure("java", configure)
