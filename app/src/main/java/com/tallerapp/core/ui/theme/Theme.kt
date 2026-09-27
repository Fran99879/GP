package com.tallerapp.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

/**
 * Aclara un color mezclándolo con blanco. En modo oscuro el acento elegido se usa
 * aclarado: Material espera un `primary` claro sobre fondo oscuro, y los acentos de la
 * paleta son tonos medios que sobre navy quedarían sin contraste.
 */
private fun aclarar(color: Color, factor: Float): Color = Color(
    red = color.red + (1f - color.red) * factor,
    green = color.green + (1f - color.green) * factor,
    blue = color.blue + (1f - color.blue) * factor,
    alpha = color.alpha,
)

@Composable
fun TallerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val base = if (darkTheme) DarkColors else LightColors
    // Color de acento elegido por el usuario (afecta botones y resaltados).
    // Se aplica en AMBOS modos: antes se ignoraba en oscuro y el selector no hacía nada.
    val acento = Color(TemaApp.accentColor)
    val esquema = if (darkTheme) {
        val claro = aclarar(acento, 0.45f)
        base.copy(primary = claro, onPrimary = Navy, secondary = claro, tertiary = acento)
    } else {
        base.copy(primary = acento, onPrimary = White, secondary = acento)
    }

    // Con targetSdk 35+ la app dibuja debajo de las barras del sistema (edge-to-edge).
    // Hay que decirle al sistema si sus íconos deben ser oscuros o claros, o quedan
    // invisibles (blanco sobre blanco en tema claro).
    val vista = LocalView.current
    if (!vista.isInEditMode) {
        SideEffect {
            val ventana = (vista.context as Activity).window
            WindowCompat.getInsetsController(ventana, vista).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = esquema,
        typography = TallerTypography,
        content = content,
    )
}
