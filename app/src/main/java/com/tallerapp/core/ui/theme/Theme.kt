package com.tallerapp.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Brand700,
    onPrimary = White,
    primaryContainer = Brand100,
    onPrimaryContainer = Brand900,
    secondary = Brand500,
    onSecondary = White,
    background = BackgroundLight,
    onBackground = Slate900,
    surface = SurfaceLight,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate200,
    error = Gasto,
    onError = White,
)

private val DarkColors = darkColorScheme(
    primary = Brand200,
    onPrimary = Brand900,
    primaryContainer = Brand700,
    onPrimaryContainer = Brand100,
    secondary = Brand200,
    onSecondary = Brand900,
    background = BackgroundDark,
    onBackground = Slate100,
    surface = SurfaceDark,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate200,
    outline = Slate700,
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
