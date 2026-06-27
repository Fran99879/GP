package com.tallerapp.core.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.tallerapp.TallerApplication

/** Acceso al contenedor de dependencias desde composables. */
@Composable
fun rememberAppContainer(): AppContainer {
    val context = LocalContext.current
    return (context.applicationContext as TallerApplication).container
}
