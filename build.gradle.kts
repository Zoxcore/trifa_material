import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.10"
    // kotlin("multiplatform") version "1.9.10"
    id("org.jetbrains.compose") version "1.5.1"
}

group = "com.zoffcc.applications.trifa_material"
version = "1.0.0-SNAPSHOT"

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
            copyright = "© 2023 Zoff. All rights reserved."
            // vendor = "Example vendor"
            // licenseFile.set(project.file("LICENSE.txt"))
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))

            val iconsRoot = project.file("resources")
            macOS {
                iconFile.set(iconsRoot.resolve("icon-mac.icns"))
            }
            windows {
                iconFile.set(iconsRoot.resolve("icon-windows.ico"))
                menuGroup = "TRIfA Material"
                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                // and https://www.guidgen.com/
                upgradeUuid = "7774da26-11dd-4ea4-bd08-f4950d252504"
            }
            linux {
                iconFile.set(iconsRoot.resolve("icon-linux.png"))
            }
        }
    }
}
