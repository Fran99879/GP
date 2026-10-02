import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.tallerapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tallerapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 9
        versionName = "1.4"

    }

    // Firma de release. Las credenciales viven en keystore.properties (fuera de git);
    // si no está, la app igual compila (queda sin firmar) para no romper a quien no lo tenga.
    val keystoreProps = Properties().apply {
        val f = rootProject.file("keystore.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }
    val hayKeystore = keystoreProps.getProperty("storeFile") != null

    signingConfigs {
        if (hayKeystore) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hayKeystore) signingConfig = signingConfigs.getByName("release")
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
    val composeBom = platform("androidx.compose:compose-bom:2025.06.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Cobro de suscripciones (plan Pro) vía Google Play.
    implementation("com.android.billingclient:billing-ktx:9.1.0")

    // Aviso de actualización disponible (In-App Updates de Play).
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // Lectura de códigos de barras con la interfaz de Google Play Services: no pide permiso
    // de CAMARA y el módulo se descarga a demanda, así que no suma peso al APK.
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")

    // SDK de licencias (módulo referenciado en settings.gradle.kts).

    // Persistencia local (Frozen Spec 6 / Arquitectura AD-4)
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")

    debugImplementation("androidx.compose.ui:ui-tooling")

    // Tests unitarios del dominio (JVM puro, sin Android).
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
