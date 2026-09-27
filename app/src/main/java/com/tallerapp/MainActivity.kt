package com.tallerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.tallerapp.core.navigation.TallerApp
import com.tallerapp.core.ui.theme.TallerAppTheme
import com.tallerapp.core.ui.theme.TemaApp
import com.tallerapp.core.ui.theme.TemaModo
import com.tallerapp.features.onboarding.OnboardingScreen

/**
 * Único punto de entrada. Monta el tema y muestra el onboarding la primera vez.
 *
 * La app abre siempre: el sistema de licencias Ed25519 quedó solo para la versión de
 * escritorio. En móvil la monetización va por Google Play Billing (planes Pro/Premium),
 * que no bloquea el arranque sino que habilita funciones.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // targetSdk 35+ fuerza edge-to-edge; se declara explícito para controlarlo.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        TemaApp.cargar(this)
        setContent {
            val oscuro = when (TemaApp.modo) {
                TemaModo.CLARO -> false
                TemaModo.OSCURO -> true
                TemaModo.SISTEMA -> isSystemInDarkTheme()
            }
            TallerAppTheme(darkTheme = oscuro) {
                val context = LocalContext.current
                var onboardingVisto by remember { mutableStateOf(TemaApp.onboardingVisto) }

                if (!onboardingVisto) {
                    OnboardingScreen(onEmpezar = {
                        TemaApp.marcarOnboardingVisto(context)
                        onboardingVisto = true
                    })
                } else {
                    TallerApp()
                }
            }
        }
    }
}
