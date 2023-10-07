pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        val composeVersion = extra["compose.version"] as String
        val multiplatformVersion = extra["multiplatform.version"] as String

        kotlin("jvm").version(kotlinVersion)
        kotlin("multiplatform").version(multiplatformVersion)
        id("org.jetbrains.compose").version(composeVersion)
    }
}

rootProject.name = "trifa_material"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.7.0")
}
