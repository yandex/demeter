plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.tools.gradle)
    implementation(libs.compose.compiler.gradlePlugin)

    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
