package plugins.module.gradle

import BuildConfig
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin

group = BuildConfig.demeterGroup
version = BuildConfig.demeterVersion

plugins.apply(MavenPublishPlugin::class.java)
