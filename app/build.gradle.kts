import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// Config del sistema de licencias. Vive en app/licensing.properties, que está gitignored:
// nunca se sube a GitHub. Si el archivo no existe, quedan valores vacíos (la app pide activación).
val licensingProps = Properties().apply {
    val f = rootProject.file("app/licensing.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun lic(key: String, default: String): String = licensingProps.getProperty(key, default)

android {
    namespace = "com.tallerapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tallerapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Config de licencias inyectada en BuildConfig (desde app/licensing.properties, fuera de git).
        buildConfigField("String", "LICENSE_PRODUCT_ID", "\"${lic("PRODUCT_ID", "mis_finanzas")}\"")
        buildConfigField("String", "LICENSE_PUBLIC_KEY", "\"${lic("PUBLIC_KEY_HEX", "")}\"")
        // Bypass de desarrollo: si es true, la app NO exige licencia (para probar sin backend).
        buildConfigField("boolean", "LICENSE_DEV_BYPASS", lic("DEV_BYPASS", "true"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // En Kotlin 2.0 el compilador de Compose se aplica con el plugin org.jetbrains.kotlin.plugin.compose
    // (ya no se usa composeOptions.kotlinCompilerExtensionVersion).

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // SDK de licencias (módulo referenciado en settings.gradle.kts).
    implementation(project(":licensesdk"))

    // Persistencia local (Frozen Spec 6 / Arquitectura AD-4)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")

    // Tests unitarios del dominio (JVM puro, sin Android).
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
