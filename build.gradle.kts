import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("org.jetbrains.compose") version "1.5.11"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.javamodularity.moduleplugin") version "1.8.15"
}

group = "com.madsky"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven("https://europe-maven.pkg.dev/jxbrowser/releases")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.java-websocket:Java-WebSocket:1.5.3")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")

    // JavaFX version 21
    implementation("org.openjfx:javafx-controls:21")
    implementation("org.openjfx:javafx-web:21")
    implementation("org.openjfx:javafx-swing:21")
    implementation("org.openjfx:javafx-fxml:21")

    // Jsoup
    implementation("org.jsoup:jsoup:1.17.2")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.web", "javafx.swing", "javafx.fxml")
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
                "src/main/resources/extra/apollo_key.txt",
                "src/main/resources/extra/LICENSE.txt",
                "src/main/resources/extra/icon.ico",
                "src/main/resources/extra/server/server.exe",
                "src/main/resources/extra/chrome"
            )

            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            description = "LinkedIn Profile Parser"
            vendor = "Madsky"
            copyright = "© 2025 Madsky. All rights reserved."
            licenseFile.set(project.file("src/main/resources/extra/LICENSE.txt"))
        }
    }
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs = listOf(
        "--add-modules", "javafx.controls,javafx.web,javafx.swing",
        "--add-opens", "javafx.web/com.sun.javafx.webkit=ALL-UNNAMED"
    )
}