package com.tallerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tallerapp.core.navigation.TallerApp
import com.tallerapp.core.ui.theme.TallerAppTheme

/**
 * Único punto de entrada de la aplicación (monodispositivo, monousuario - Frozen Spec 2.3).
 * Solo monta el tema y el grafo de navegación. Sin lógica de negocio.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TallerAppTheme {
                TallerApp()
            }
        }
    }
}
