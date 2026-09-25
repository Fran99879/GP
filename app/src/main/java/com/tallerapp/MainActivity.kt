package com.tallerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.licensemanager.sdk.core.LicenseStatus
import com.tallerapp.core.license.Licencia
import com.tallerapp.core.navigation.TallerApp
import com.tallerapp.core.ui.theme.TallerAppTheme
import com.tallerapp.core.ui.theme.TemaApp
import com.tallerapp.core.ui.theme.TemaModo
import com.tallerapp.features.licencia.ActivacionScreen
import com.tallerapp.features.onboarding.OnboardingScreen

/**
 * Único punto de entrada. Monta el tema y, según el estado de la licencia, muestra la
 * app completa o la pantalla de activación (toda la app queda bloqueada sin licencia válida).
 * El bypass de desarrollo (LICENSE_DEV_BYPASS) permite usar la app sin licencia en pruebas.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
                var habilitada by remember {
                    mutableStateOf(
                        Licencia.bypass || Licencia.sdk(context).validate() == LicenseStatus.VALID,
                    )
                }

                var onboardingVisto by remember { mutableStateOf(TemaApp.onboardingVisto) }

                when {
                    !habilitada -> ActivacionScreen(onActivada = { habilitada = true })
                    !onboardingVisto -> OnboardingScreen(onEmpezar = {
                        TemaApp.marcarOnboardingVisto(context)
                        onboardingVisto = true
                    })
                    else -> TallerApp()
                }
            }
        }
    }
}
