import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.de.undercouch.gradle.tasks.download.Download

plugins {
    kotlin("jvm")
    // kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.zoffcc.applications.trifa_material"
version = "1.0.0"
val appName = "trifa_material"

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
    implementation(compose.desktop.common)
    implementation(compose.ui)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material)
    implementation(compose.material3)
    @Suppress("OPT_IN_IS_NOT_ENABLED")
    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    implementation(compose.components.resources)
    //
    implementation(compose.materialIconsExtended)
    //
    //
    implementation("org.xerial:sqlite-jdbc:3.43.2.1")
    implementation("ca.gosyer:kotlin-multiplatform-appdirs:1.1.1")
}

compose.desktop {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    kotlin {
        jvmToolchain(17)
    }

    application {
        mainClass = "MainKt"
        // jvmArgs += listOf("-Xmx2G")
        // args += listOf("-customArgument")
        jvmArgs += listOf("-Dcom.apple.mrj.application.apple.menu.about.name=TRIfA")
        jvmArgs += listOf("-Dapple.awt.application.name=TRIfA")

        buildTypes.release.proguard {
            optimize.set(false)
            obfuscate.set(false)
            configurationFiles.from("proguard-rules.pro")
        }

        nativeDistributions {
            packageName = appName
            packageVersion = "${project.version}"
            println("packageVersion=$packageVersion")
            description = "TRIfA Material App"
            copyright = "© 2023 Zoff. All rights reserved."
            vendor = "Zoxcore"
            licenseFile.set(project.file("LICENSE"))
            println("licenseFile=" + project.file("LICENSE"))
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))

            targetFormats(
                TargetFormat.Msi, TargetFormat.Exe,
                TargetFormat.Dmg,
                TargetFormat.Deb, TargetFormat.Rpm
            ) // , TargetFormat.AppImage)

            val iconsRoot = project.file("resources")
            println("iconsRoot=$iconsRoot")
            macOS {
                println("iconFile=" + iconsRoot.resolve("icon-mac.icns"))
                iconFile.set(iconsRoot.resolve("icon-mac.icns"))
            }
            windows {
                iconFile.set(iconsRoot.resolve("icon-windows.ico"))
                println("iconFile=" + iconsRoot.resolve("icon-windows.ico"))
                menuGroup = "TRIfA Material"
                // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
                // and https://www.guidgen.com/
                upgradeUuid = "7774da26-11dd-4ea4-bd08-f4950d252504"
            }
            linux {
                iconFile.set(iconsRoot.resolve("icon-linux.png"))
                println("iconFile=" + iconsRoot.resolve("icon-linux.png"))
            }
        }
    }
}

val appImageTool = project.file("deps/appimagetool.AppImage")
val linuxAppDir = project.file("build/compose/binaries/main/app")
val desktopFile = project.file("resources/trifa_material.desktop")
val linuxIconFile = project.file("resources/icon-linux.png")

tasks {
    val downloadAppImageBuilder by registering(Download::class) {
        src("https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage")
        dest(appImageTool)
        overwrite(false)
        doFirst {
            exec {
                commandLine("mkdir", "-p", "deps/")
            }
        }
        doLast {
            exec {
                commandLine("chmod", "+x", "deps/appimagetool.AppImage")
            }
        }
    }

    val copyAppimageDesktopfile by registering(Exec::class) {
        environment("ARCH", "x86_64")
        commandLine("cp", "-v", desktopFile, linuxAppDir)
    }

    val copyAppimageIconfile by registering(Exec::class) {
        environment("ARCH", "x86_64")
        commandLine("cp", "-v", linuxIconFile, "${linuxAppDir}/${appName}")
    }

    val executeAppImageBuilder by registering(Exec::class) {
        dependsOn(downloadAppImageBuilder)
        // dependsOn(copyBuildToPackaging)
        dependsOn(copyAppimageDesktopfile)
        dependsOn(copyAppimageIconfile)
        environment("ARCH", "x86_64")
        commandLine(appImageTool, linuxAppDir, "$appName-${project.version}.AppImage")
    }
}
