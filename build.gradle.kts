// Top-level build file. Plugin versions are declared here and applied per-module.
plugins {
    id("com.android.application") version "9.3.3" apply false
    id("com.android.library") version "9.3.3" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
    // KSP 2.0.21-1.0.25: última que conserva KSP1 (Room 2.6.1 no soporta KSP2).
    id("com.google.devtools.ksp") version "2.3.6" apply false
}
