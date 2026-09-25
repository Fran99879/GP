package com.tallerapp.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tallerapp.core.ui.theme.Azul
import com.tallerapp.core.ui.theme.MetaCompleta
import com.tallerapp.core.ui.theme.Navy
import com.tallerapp.core.ui.theme.Tostado

/**
 * Sistema de diseño compartido con la app de escritorio.
 *
 * El look del escritorio es **flat**: superficie blanca, borde cálido de 1px, radio 12
 * y **sin sombra**. Estos componentes replican esos tokens para que ambas apps se vean igual.
 * Ver `UI-ESCRITORIO-ANALISIS-Y-ROADMAP-MOVIL.md`.
 */

/** Radio y padding estándar de tarjeta (equivale al estilo `Card` de WPF). */
private val RadioTarjeta = RoundedCornerShape(12.dp)
private val PaddingTarjeta = 16.dp

/** Tarjeta flat: borde `outline` 1px, sin elevación (como el escritorio). */
@Composable
fun TarjetaApp(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    padding: androidx.compose.ui.unit.Dp = PaddingTarjeta,
    conBorde: Boolean = true,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RadioTarjeta,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (conBorde) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
    ) {
        Column(modifier = Modifier.padding(padding), content = contenido)
    }
}

/** Título de pantalla (equivale al estilo `H1`: 22sp bold). */
@Composable
fun TituloPantalla(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        modifier = modifier,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

/** Título de sección (equivale al estilo `H2`: 16sp semibold). */
@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

/** Texto secundario/label (equivale al estilo `Muted`: 13sp, color apagado). */
@Composable
fun TextoMuted(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Botón secundario con contorno (equivale al estilo `Ghost`). */
@Composable
fun BotonGhost(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text(texto, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Tarjeta de métrica del dashboard: label arriba, cifra grande abajo.
 * `destacada = true` usa el fondo navy con texto tostado (tarjeta "Balance" del escritorio).
 */
@Composable
fun TarjetaMetrica(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    colorValor: Color = MaterialTheme.colorScheme.onSurface,
    destacada: Boolean = false,
    subtitulo: String? = null,
) {
    // En claro la tarjeta destacada es navy sobre tarjetas blancas (como el escritorio).
    // En oscuro el navy se confunde con el fondo, así que se usa el azul, que sí contrasta.
    val temaOscuro = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val colorDestacado = if (temaOscuro) Azul else Navy
    TarjetaApp(
        modifier = modifier,
        color = if (destacada) colorDestacado else MaterialTheme.colorScheme.surface,
        conBorde = !destacada,
    ) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = if (destacada) Tostado else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            valor,
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (destacada) Color.White else colorValor,
        )
        if (subtitulo != null) {
            Text(
                subtitulo,
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = if (destacada) Tostado else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Bloque de resumen de 3 métricas sobre fondo cálido (`TostadoClaro`),
 * igual al de Movimientos/Reportes del escritorio.
 */
@Composable
fun BloqueResumen3(
    modifier: Modifier = Modifier,
    contenido: @Composable RowScope.() -> Unit,
) {
    TarjetaApp(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        conBorde = false,
        padding = 18.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = contenido,
        )
    }
}

/** Una métrica dentro de [BloqueResumen3]. */
@Composable
fun RowScope.MetricaResumen(etiqueta: String, valor: String, color: Color) {
    Column(modifier = Modifier.weight(1f)) {
        TextoMuted(etiqueta)
        Text(valor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = color)
    }
}

/**
 * Fila de meta de ahorro con barra de progreso, igual a la del Dashboard del escritorio:
 * "🎯 Nombre — actual / objetivo (pct%)" y una barra de 12dp (verde si está completa).
 */
@Composable
fun FilaMeta(
    nombre: String,
    textoMontos: String,
    fraccion: Float,
    modifier: Modifier = Modifier,
) {
    val completa = fraccion >= 1f
    val color = if (completa) MetaCompleta else MaterialTheme.colorScheme.primary
    TarjetaApp(modifier = modifier.fillMaxWidth()) {
        Text(
            "${if (completa) "✅" else "🎯"}  $nombre  —  $textoMontos  (${(fraccion * 100).toInt()}%)",
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.outline),
        ) {
            if (fraccion > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(fraccion.coerceIn(0.0001f, 1f))
                        .background(color),
                )
            }
            if (fraccion < 1f) {
                Box(modifier = Modifier.weight((1f - fraccion).coerceIn(0.0001f, 1f)))
            }
        }
    }
}
