import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("org.jetbrains.compose") version "1.7.3"
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

    // Kotlin X
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("org.java-websocket:Java-WebSocket:1.5.3")

    // Google
    implementation("com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")

    // Slf4j
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    // JavaFX version 17
    implementation("org.openjfx:javafx-graphics:17")
    implementation("org.openjfx:javafx-controls:17")
    implementation("org.openjfx:javafx-fxml:17")
    implementation("org.openjfx:javafx-base:17")
    implementation("org.openjfx:javafx-swing:17")
    implementation("org.openjfx:javafx-web:17")
    runtimeOnly("org.openjfx:javafx-graphics:17:win")
    runtimeOnly("org.openjfx:javafx-base:17:win")
    runtimeOnly("org.openjfx:javafx-controls:17:win")
    runtimeOnly("org.openjfx:javafx-fxml:17:win")
    runtimeOnly("org.openjfx:javafx-swing:17:win")
    runtimeOnly("org.openjfx:javafx-web:17:win")

    // OkHttp3
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSONObject
    implementation("org.json:json:20250107")

    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.7.3")
}

javafx {
    version = "17"
    modules = listOf("javafx.graphics", "javafx.controls", "javafx.web", "javafx.swing", "javafx.fxml")
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
        "--add-modules", "javafx.graphics,javafx.controls,javafx.web,javafx.swing",
        "--add-opens", "javafx.web/com.sun.javafx.webkit=ALL-UNNAMED"
    )
}