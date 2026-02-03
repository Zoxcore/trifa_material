@file:Suppress("SpellCheckingInspection", "ConvertToStringTemplate", "PropertyName", "LocalVariableName")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import de.undercouch.gradle.tasks.download.Download
import org.ajoberstar.grgit.Grgit
import java.time.format.DateTimeFormatter
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    kotlin("jvm")
    // kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.github.gmazzo.buildconfig") version "6.0.7"
    id("org.ajoberstar.grgit") version "5.3.3"
    id("io.gitlab.arturbosch.detekt") version("1.23.3")
    id("de.undercouch.download") version "5.6.0"
}

group = "com.zoffcc.applications.trifa_material"
version = "1.0.59"
val appName = "trifa_material"

val build_with_appimage = false

var os: OperatingSystem? = null
var os_arch: String? = null
var os_java_home: String? = null
var os_java_runtime_version: String? = null
var os_java_vm_version: String? = null

try
{
    os = OperatingSystem.current()
    os_arch = System.getProperty("os.arch")
    os_java_home = System.getProperty("java.home")
    os_java_runtime_version = System.getProperty("java.runtime.version")
    os_java_vm_version = System.getProperty("java.vm.version")

    println("*** Building on ${os!!.familyName} / ${os!!.name} / ${os!!.version} / ${System.getProperty("os.arch")}.")
    println("*** os_java_home: $os_java_home.")
    println("*** os_java_runtime_version: $os_java_runtime_version.")
    println("*** os_java_vm_version: $os_java_vm_version.")
}
catch(_: Exception)
{
    println("some Error detecting OS for Java")
}

repositories {
    flatDir {
        dirs("customlibs")
    }
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven("https://jitpack.io")
}

detekt {
    config.setFrom(file("config/detekt/detekt.yml"))
}

buildConfig {
    buildConfigField("String", "APP_NAME", "\"${project.name}\"")
    buildConfigField("String", "APP_VERSION", provider { "\"${project.version}\"" })
    buildConfigField("String", "PROJECT_VERSION", "\"${project.version}\"")
    buildConfigField("String", "KOTLIN_VERSION", "\"${kotlin.coreLibrariesVersion}\"")
    buildConfigField("String", "COMPOSE_VERSION", "\"${project.findProperty("compose.version")}\"")
    try
    {
        val grgit = if (extra.has("grgit")) null else the<Grgit>()
        try
        {
            buildConfigField("String", "GIT_BRANCH", "\"" + grgit!!.branch.current().fullName + "\"")
        }
        catch (_: Exception)
        {
            buildConfigField("String", "GIT_BRANCH", "\"" + "????" + "\"")
        }
        buildConfigField("String", "GIT_COMMIT_HASH", "\"" + grgit!!.head().abbreviatedId + "\"")
        buildConfigField("String", "GIT_COMMIT_DATE", "\"" + grgit.head().dateTime.
          format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\"")
        buildConfigField("String", "GIT_COMMIT_MSG", "\"" + grgit.head().shortMessage.
          replace("\"", "_").replace("\n", "_").
          replace("\\", "_").
          replace("\r", "_").take(40) + "\"")
    }
    catch (_: Exception)
    {
        try
        {
            buildConfigField("String", "GIT_BRANCH", "\"" + "????" + "\"")
        }
        catch (_: Exception)
        {
        }
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
    @Suppress("DEPRECATION")
    implementation(compose.desktop.common)
    @Suppress("DEPRECATION")
    implementation(compose.ui)
    @Suppress("DEPRECATION")
    implementation(compose.runtime)
    @Suppress("DEPRECATION")
    implementation(compose.foundation)
    @Suppress("DEPRECATION")
    implementation(compose.material)
    @Suppress("DEPRECATION")
    implementation(compose.material3)
    @Suppress("OPT_IN_IS_NOT_ENABLED", "DEPRECATION")
    @OptIn(ExperimentalComposeLibrary::class)
    implementation(compose.components.resources)
    //
    @Suppress("DEPRECATION")
    implementation(compose.materialIconsExtended)
    //
    //
    // # some hints to detect nixOS java runtime
    //
    // sun.boot.library.path=/nix/store/p9mylq323pjrabd7w0fhgh1yb8...
    // java.home=/nix/store/p9mylq323pjrabd7w0fhgh1yb8...
    // java.runtime.version=21.0.8+9-nixos
    // java.vm.version=21.0.8+9-nixos
    //
    var running_on_nixos = false
    try
    {
        if (os_java_runtime_version!!.contains("nixos"))
        {
            running_on_nixos = true
        }
        else if (os_java_vm_version!!.contains("nixos"))
        {
            running_on_nixos = true
        }
    }
    catch(_: Exception)
    {
    }
    // # some hints to detect nixOS java runtime
    //
    // ------- SQLITE / SQLCIPHER implementation -------
    try
    {
        if ((os!!.isLinux) && (os_arch == "amd64") && (!running_on_nixos) && (!build_with_appimage))
        {
            // on "Linux amd64" use "sqlite-jdbc" with sqlcipher included (which is a dropin replacement for sqlite-jdbc)
            println("Linux amd64 -> unsing pkgs_zoffcc_sqlite-jdbc-sqlcipher")
            implementation("com.github.zoff99:pkgs_zoffcc_sqlite-jdbc-sqlcipher:1.0.22")
        } else
        {
            // use regular "sqlite-jdbc"
            println("other OS -> unsing org.xerial:sqlite-jdbc")
            implementation("org.xerial:sqlite-jdbc:3.51.1.0")
        }
    }
    catch(_: Exception)
    {
        // use regular "sqlite-jdbc"
        println("error detecting OS -> unsing org.xerial:sqlite-jdbc")
        implementation("org.xerial:sqlite-jdbc:3.51.1.0")
    }
    // ------- SQLITE / SQLCIPHER implementation -------
    //
    //
    //
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("ca.gosyer:kotlin-multiplatform-appdirs:1.2.0")
    implementation("com.sksamuel.scrimage:scrimage-core:4.3.5")
    implementation("com.sksamuel.scrimage:scrimage-webp:4.3.5")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("io.github.alexzhirkevich:qrose:1.1.1")
    implementation("com.vanniktech:emoji-ios:0.21.0")
    // implementation("io.github.theapache64:rebugger:1.0.0-rc02")
}

val main_class_name = "TrifaMainKt"

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

        mainClass = main_class_name
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

            if (build_with_appimage)
            {
                println("#### build with AppImage ####")
                targetFormats(
                    TargetFormat.Msi, TargetFormat.Exe,
                    TargetFormat.Dmg,
                    TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage
                )
            }
            else
            {
                println("==== build without AppImage ====")
                targetFormats(
                    TargetFormat.Msi, TargetFormat.Exe,
                    TargetFormat.Dmg,
                    TargetFormat.Deb, TargetFormat.Rpm
                )
            }

            nativeDistributions {
                modules("java.instrument", "java.net.http", "java.prefs", "java.sql", "jdk.unsupported", "jdk.security.auth")
                // includeAllModules = true
            }

            val iconsRoot = project.file("resources")
            println("iconsRoot=$iconsRoot")
            macOS {
                // --- scrimage needs this set ONLY for macos arm
                // --- scrimage needs this set ONLY for macos arm
                // jvmArgs += listOf("-Dcom.sksamuel.scrimage.webp.platform=mac-arm64")
                // --- scrimage needs this set ONLY for macos arm
                // --- scrimage needs this set ONLY for macos arm
                println("iconFile=" + iconsRoot.resolve("icon-mac.icns"))
                iconFile.set(iconsRoot.resolve("icon-mac.icns"))
                bundleID = "com.zoffcc.applications.trifamaterial"
                // HINT: https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Signing_and_notarization_on_macOS/README.md
                signing {
                    sign.set(false)
                    identity.set("Rupert Key")
                    keychain.set("keychain/macos_keychain")
                }
                //notarization {
                //    val providers = project.providers
                //    appleID.set(providers.environmentVariable("NOTARIZATION_APPLE_ID"))
                //    password.set(providers.environmentVariable("NOTARIZATION_PASSWORD"))
                //}
                runtimeEntitlementsFile.set(iconsRoot.resolve("runtime-entitlements.plist"))
                infoPlist {
                    extraKeysRawXml = macExtraPlistKeys
                }
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

            // XX // jvmArgs += "-splash:resources/splash_screen.png"
            // XX // jvmArgs += "-splash:${'$'}APPDIR/app/resources/splash_screen.png"
            // XX // jvmArgs += "-splash:" + iconsRoot.resolve("splash_screen.png")
            // -----------------------------------------------------------------
            // --> for .deb -->
            jvmArgs += "-splash:${'$'}APPDIR/resources/splash_screen.png"
            // --> for gradlew run --> // jvmArgs += "-splash:resources/splash_screen.png"
            // -----------------------------------------------------------------
            println("jvmArgs=" + jvmArgs)
            // val ENV = System.getenv()
            // println("ENV_all=" + ENV.keys)
        }
    }
}

val macExtraPlistKeys: String
// HINT: do NOT remove the formatting or the indents ----------
    get() = """    <key>NSMicrophoneUsageDescription</key>
    <string>Need microphone access for making audio calls</string>
    <key>NSCameraUsageDescription</key>
    <string>Need camera access for making video calls</string>
    <key>NSPhotoLibraryAddUsageDescription</key>
    <string>Need photo library access for saving and uploading images</string>
    <key>NSPhotoLibraryUsageDescription</key>
    <string>Need photo library access for saving and uploading images</string>"""
// HINT: do NOT remove the formatting or the indents ----------

val appImageTool = project.file("deps/appimagetool.AppImage")
val linuxAppDir = project.file("build/compose/binaries/main/app")
val desktopFile = project.file("resources/trifa_material.desktop")
val linuxIconFile = project.file("resources/icon-linux.png")

tasks.withType<org.gradle.jvm.tasks.Jar> {
    manifest {
        attributes["SplashScreen-Image"] = "splash_screen.png"
    }
}

tasks {
    val downloadAppImageBuilder by registering(Download::class) {
        src("https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage")
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
        @Suppress("RemoveSingleExpressionStringTemplate", "RemoveCurlyBracesFromTemplate")
        println("appName=" + "${appName}")
        commandLine("cp", "-v", linuxIconFile, "${linuxAppDir}/trifa_material.png")
    }

    val setAppimageRunfile by registering(Exec::class) {
        workingDir = linuxAppDir
        commandLine("ln", "-sf", "trifa_material/bin/trifa_material", "AppRun")
    }

    @Suppress("unused") val executeAppImageBuilder by registering(Exec::class) {
        dependsOn(downloadAppImageBuilder)
        dependsOn(copyAppimageDesktopfile)
        dependsOn(copyAppimageIconfile)
        dependsOn(setAppimageRunfile)
        environment("ARCH", "x86_64")
        @Suppress("RemoveCurlyBracesFromTemplate")
        println("cmd: " + "${appImageTool} ${linuxAppDir} $appName-${project.version}-x86_64.AppImage")
        commandLine(appImageTool, linuxAppDir, "$appName-${project.version}-x86_64.AppImage")
    }
}
