plugins {
    kotlin("multiplatform") apply false
    id("org.jetbrains.compose") apply false
    id("org.jetbrains.kotlin.plugin.compose") apply true
    id("org.jetbrains.kotlin.plugin.serialization") apply false
}

group = "com.madsky"
version = "1.0-SNAPSHOT"