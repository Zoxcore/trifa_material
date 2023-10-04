import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.compose") version "1.5.0"
}

group = "com.zoffcc.applications.trifa_material"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "com.zoffcc.applications.trifa.MainKt"
        // jvmArgs += listOf("-Xmx2G")
        // args += listOf("-customArgument")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "trifa_material"
            packageVersion = "1.0.0"
            // description = "Compose Example App"
            // copyright = "© 2020 My Name. All rights reserved."
            // vendor = "Example vendor"
            // licenseFile.set(project.file("LICENSE.txt"))
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
        }
    }
}
