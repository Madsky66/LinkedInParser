import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        freeCompilerArgs.add("-Xwhen-guards")
        freeCompilerArgs.add("-Xnon-local-break-continue")
        freeCompilerArgs.add("-Xmulti-dollar-interpolation")
    }
    jvm {
        withJava()
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    sourceSets {
        val jvmMain by getting{
            dependencies {
                // Compose
                implementation(compose.desktop.currentOs)
                implementation(libs.compose.material)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.ui)

                // Kotlin X
                implementation(libs.kotlin.serialization)
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.kotlin.coroutines.swing)
                implementation(libs.kotlin.stdlib)

                // Material Icons Extended
                implementation(libs.material.icons.extended)

                // Slf4j
                implementation(libs.slf4j.api)
                implementation(libs.slf4j.simple)

                // OkHttp3
                implementation(libs.okhttp)

                // Java WebSocket
                implementation(libs.java.websocket)

                // JSONObject
                implementation(libs.json)

                // Apache POI
                implementation(libs.apache.poi)
                implementation(libs.apache.poi.ooxml)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "LinkedInParser"
            packageVersion = "1.0.0"

            windows {
                menuGroup = "LinkedInParser"
                shortcut = true
                iconFile.set(project.file("src/main/resources/extra/icon.ico"))
                upgradeUuid = "938f329d-3585-430d-bbca-304ff14f3dda"
                dirChooser = true
                perUserInstall = true
            }

            fromFiles(
                "src/main/resources/LICENSE.txt",
                "src/main/resources/icon.ico"
            )

            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            description = "LinkedIn Profile Parser"
            vendor = "Madsky"
            copyright = "© 2025 Madsky. All rights reserved."
            licenseFile.set(project.file("src/main/resources/extra/LICENSE.txt"))
        }
    }
}