plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {}
    jvmToolchain(17)

    sourceSets["jvmMain"].dependencies {
        // Compose
        implementation(compose.desktop.currentOs)
        implementation(compose.components.resources)
    }

    sourceSets["commonMain"].dependencies {
        // Kotlin X
        implementation(libs.kotlin.stdlib)
        implementation(libs.kotlin.serialization)
        implementation(libs.kotlin.coroutines.core)
        implementation(libs.kotlin.coroutines.swing)

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

        compilerOptions {
            freeCompilerArgs.add("-Xwhen-guards")
            freeCompilerArgs.add("-Xnon-local-break-continue")
            freeCompilerArgs.add("-Xmulti-dollar-interpolation")
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi)
            packageName = "LinkedInParser"
            packageVersion = "1.0.0"

            windows {
                menuGroup = "LinkedInParser"
                shortcut = true
                iconFile.set(project.file("src/jvmMain/composeResources/drawable/icon.ico"))
                upgradeUuid = "938f329d-3585-430d-bbca-304ff14f3dda"
                dirChooser = true
                perUserInstall = true
            }

            fromFiles(
//                "src/jvmMain/composeResources/LICENSE.txt",
                "src/jvmMain/composeResources/drawable/icon.ico"
            )

            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/jvmMain/composeResources"))
            description = "LinkedIn Profile Parser"
            vendor = "Madsky"
            copyright = "© 2025 Madsky. All rights reserved."
//            licenseFile.set(project.file("src/jvmMain/composeResources/LICENSE.txt"))
        }
    }
}