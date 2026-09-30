package com.tallerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tallerapp.core.actualizacion.ActualizacionApp
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

    private lateinit var actualizacion: ActualizacionApp

    override fun onCreate(savedInstanceState: Bundle?) {
        // targetSdk 35+ fuerza edge-to-edge; se declara explícito para controlarlo.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        TemaApp.cargar(this)
        actualizacion = ActualizacionApp(this)
        setContent {
            val oscuro = when (TemaApp.modo) {
                TemaModo.CLARO -> false
                TemaModo.OSCURO -> true
                TemaModo.SISTEMA -> isSystemInDarkTheme()
            }
            TallerAppTheme(darkTheme = oscuro) {
                val context = LocalContext.current
                var onboardingVisto by remember { mutableStateOf(TemaApp.onboardingVisto) }

                // Aviso de actualización: Play descarga de fondo y acá se ofrece reiniciar.
                val avisos = remember { SnackbarHostState() }
                var actualizacionLista by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    actualizacion.descargaLista = { actualizacionLista = true }
                    actualizacion.buscar(this@MainActivity)
                }
                LaunchedEffect(actualizacionLista) {
                    if (!actualizacionLista) return@LaunchedEffect
                    val elegido = avisos.showSnackbar(
                        message = "Hay una versión nueva lista para instalar",
                        actionLabel = "Reiniciar",
                        duration = SnackbarDuration.Indefinite,
                    )
                    if (elegido == SnackbarResult.ActionPerformed) actualizacion.completar()
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (!onboardingVisto) {
                        OnboardingScreen(onEmpezar = {
                            TemaApp.marcarOnboardingVisto(context)
                            onboardingVisto = true
                        })
                    } else {
                        TallerApp()
                    }
                    SnackbarHost(avisos, modifier = Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    }

    override fun onDestroy() {
        if (::actualizacion.isInitialized) actualizacion.soltar()
        super.onDestroy()
    }
}
