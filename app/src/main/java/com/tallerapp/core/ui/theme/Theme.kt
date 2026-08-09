package com.tallerapp.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Tema claro: azul de marca como primario, contenedores cálidos (tostado), acento marrón.
private val LightColors = lightColorScheme(
    primary = Azul,
    onPrimary = White,
    primaryContainer = TostadoClaro,
    onPrimaryContainer = Navy,
    secondary = Marron,
    onSecondary = White,
    tertiary = Tostado,
    onTertiary = Navy,
    background = FondoClaro,
    onBackground = Navy,
    surface = White,
    onSurface = Navy,
    surfaceVariant = GrisCalido200,
    onSurfaceVariant = GrisCalido700,
    outline = GrisCalido200,
    error = Gasto,
    onError = White,
)

// Tema oscuro: fondo azul marino profundo, acentos tostado/tan.
private val DarkColors = darkColorScheme(
    primary = Tostado,
    onPrimary = Navy,
    primaryContainer = Azul,
    onPrimaryContainer = Tostado,
    secondary = Tostado,
    onSecondary = Navy,
    tertiary = Marron,
    onTertiary = White,
    background = NavyProfundo,
    onBackground = BlancoCalido,
    surface = SuperficieOscura,
    onSurface = BlancoCalido,
    surfaceVariant = Navy,
    onSurfaceVariant = GrisCalido200,
    outline = Azul,
    error = Gasto,
    onError = White,
)

@Composable
fun TallerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = TallerTypography,
        content = content,
    )
}
