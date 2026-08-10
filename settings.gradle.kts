pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TallerApp"
include(":app")

// SDK de licencias: se referencia desde su proyecto original (no se copia ni se sube a este repo).
include(":licensesdk")
project(":licensesdk").projectDir =
    file("../ProyectoDeLicenciasParaApk/android-license-sdk/licensesdk")
