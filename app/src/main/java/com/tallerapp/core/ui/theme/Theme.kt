package com.tallerapp.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Esquema fijo claro. Modo oscuro está fuera de alcance del MVP (Frozen Spec 4).
private val TallerLightColors = lightColorScheme(
    primary = Blue700,
    onPrimary = White,
    secondary = Blue500,
    background = GreyBg,
    surface = White,
    onBackground = Grey900,
    onSurface = Grey900,
)

@Composable
fun TallerAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TallerLightColors,
        typography = TallerTypography,
        content = content,
    )
}
