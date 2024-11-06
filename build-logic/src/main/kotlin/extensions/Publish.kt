package extensions

import BuildConfig
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.maven
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin

fun Project.publishLib(
    artifactId: String,
    isPluginProject: Boolean = false,
) {
    plugins.apply(MavenPublishPlugin::class.java)

    if (isPluginProject) {
        return configurePublishRepositories()
    }

    when {
        plugins.hasPlugin(JavaPlugin::class.java) -> {
            configurePublishingLib(artifactId, components["java"])
        }

        plugins.hasPlugin(AndroidBasePlugin::class.java) -> {
            androidLib {
                publishing {
                    singleVariant("release") {
                        withSourcesJar()
                    }
                }
            }
            afterEvaluate {
                configurePublishingLib(artifactId, components["release"])
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

                from(component)
            }
        }
    }
    configurePublishRepositories()
    configurePublishSigning()
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

    publishing {
        signing {
            project.extra["signing.keyId"] = keyId
            project.extra["signing.password"] = password
            project.extra["signing.secretKeyRingFile"] = ringFile
            sign(publications["maven"])
        }
    }
}

internal fun Project.publishing(configure: org.gradle.api.Action<PublishingExtension>): Unit = extensions.configure("publishing", configure)

internal fun Project.signing(configure: org.gradle.api.Action<SigningExtension>): Unit = extensions.configure("signing", configure)
