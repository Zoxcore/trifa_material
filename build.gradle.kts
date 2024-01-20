import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.de.undercouch.gradle.tasks.download.Download
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm")
    // kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.github.gmazzo.buildconfig") version "4.2.0"
    id("org.ajoberstar.grgit") version "4.1.0"
}

group = "com.zoffcc.applications.trifa_material"
version = "1.0.17"
val appName = "trifa_material"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_VERSION", provider { "\"${project.version}\"" })
    buildConfigField("String", "PROJECT_VERSION", "\"${project.version}\"")
    buildConfigField("String", "KOTLIN_VERSION", "\"${kotlin.coreLibrariesVersion}\"")
    buildConfigField("String", "COMPOSE_VERSION", "\"${project.findProperty("compose.version")}\"")
    try
    {
        try
        {
            buildConfigField("String", "GIT_BRANCH", "\"" + grgit.branch.current().fullName + "\"")
        }
        catch (e: Exception)
        {
            buildConfigField("String", "GIT_BRANCH", "\"" + "????" + "\"")
        }
        buildConfigField("String", "GIT_COMMIT_HASH", "\"" + grgit.head().abbreviatedId + "\"")
        buildConfigField("String", "GIT_COMMIT_DATE", "\"" + grgit.head().dateTime.
          format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\"")
        buildConfigField("String", "GIT_COMMIT_MSG", "\"" + grgit.head().shortMessage.
          replace("\"", "_").replace("\n", "_").
          replace("\r", "_").take(40) + "\"")
    }
    catch (e: Exception)
    {
        buildConfigField("String", "GIT_BRANCH", "\"" + "????" + "\"")
        buildConfigField("String", "GIT_COMMIT_HASH", "\"" + "????" + "\"")
        buildConfigField("String", "GIT_COMMIT_DATE", "\"" + "????" + "\"")
        buildConfigField("String", "GIT_COMMIT_MSG", "\"" + "????" + "\"")
    }
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
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("ca.gosyer:kotlin-multiplatform-appdirs:1.1.1")
    implementation("com.sksamuel.scrimage:scrimage-core:4.1.1")
    implementation("com.sksamuel.scrimage:scrimage-webp:4.1.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.github.alexzhirkevich:qrose:1.0.0-beta3")
    implementation("com.vanniktech:emoji-ios:0.18.0")
    // implementation("io.github.theapache64:rebugger:1.0.0-rc02")
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
            )
            // TargetFormat.AppImage

            nativeDistributions {
                // modules("java.instrument", "java.net.http", "java.prefs", "java.sql", "jdk.unsupported")
                includeAllModules = true
            }

            val iconsRoot = project.file("resources")
            println("iconsRoot=$iconsRoot")
            macOS {
                println("iconFile=" + iconsRoot.resolve("icon-mac.icns"))
                iconFile.set(iconsRoot.resolve("icon-mac.icns"))
                bundleID = "com.zoffcc.applications.trifa_material"
                // dockName = ""
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
            println("targetFormats=" + targetFormats)
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
        println("iconFile_src=" + linuxIconFile)
        println("iconFile_dst=" + "${linuxAppDir}/trifa_material.png")
        println("appName=" + "${appName}")
        commandLine("cp", "-v", linuxIconFile, "${linuxAppDir}/trifa_material.png")
    }

    val setAppimageRunfile by registering(Exec::class) {
        workingDir = linuxAppDir
        commandLine("ln", "-sf", "trifa_material/bin/trifa_material", "AppRun")
    }

    val executeAppImageBuilder by registering(Exec::class) {
        dependsOn(downloadAppImageBuilder)
        dependsOn(copyAppimageDesktopfile)
        dependsOn(copyAppimageIconfile)
        dependsOn(setAppimageRunfile)
        environment("ARCH", "x86_64")
        println("cmd: " + "${appImageTool} ${linuxAppDir} $appName-${project.version}-x86_64.AppImage")
        commandLine(appImageTool, linuxAppDir, "$appName-${project.version}-x86_64.AppImage")
    }
}
