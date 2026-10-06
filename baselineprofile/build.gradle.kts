plugins {
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
    id("androidx.baselineprofile")
}

android {
    namespace = "com.tallerapp.baselineprofile"
    compileSdk = 36

    defaultConfig {
        // El generador corre con Macrobenchmark, que necesita API 28 o más.
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Macrobenchmark se niega a medir en un emulador, y con razón: los tiempos no son los de
        // un teléfono. Acá se miden igual porque lo que interesa es comparar la misma app con y
        // sin Baseline Profile en la misma máquina, no publicar un número absoluto.
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    targetProjectPath = ":app"
}

// El perfil se genera sobre una variante no ofuscada: si no, los nombres de clase del perfil
// no coinciden con los del APK de release.
baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation("androidx.test.ext:junit:1.2.1")
    implementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
    implementation("androidx.benchmark:benchmark-macro-junit4:1.4.1")
}
