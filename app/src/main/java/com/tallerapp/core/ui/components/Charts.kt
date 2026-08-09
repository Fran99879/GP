package com.tallerapp.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tallerapp.core.util.Dinero

/** Una porción del gráfico de torta/dona. */
data class Porcion(val etiqueta: String, val valorCentavos: Long, val color: Color)

/**
 * Gráfico de dona con leyenda al costado. Muestra el peso relativo de cada porción.
 * Si no hay datos, no dibuja nada (el llamador decide el vacío).
 */
@Composable
fun GraficoDona(
    porciones: List<Porcion>,
    modifier: Modifier = Modifier,
) {
    val total = porciones.sumOf { it.valorCentavos }.coerceAtLeast(1)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Canvas(modifier = Modifier.size(128.dp)) {
            val grosor = size.minDimension * 0.22f
            val diametro = size.minDimension - grosor
            val topLeft = Offset((size.width - diametro) / 2f, (size.height - diametro) / 2f)
            val arcSize = Size(diametro, diametro)
            var inicio = -90f
            porciones.forEach { p ->
                val barrido = 360f * (p.valorCentavos.toFloat() / total.toFloat())
                drawArc(
                    color = p.color,
                    startAngle = inicio,
                    sweepAngle = barrido - 1.5f, // pequeño gap entre porciones
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = grosor, cap = StrokeCap.Butt),
                )
                inicio += barrido
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            porciones.forEach { p ->
                val pct = (100.0 * p.valorCentavos / total).toInt()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(p.color),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = p.etiqueta,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

/** Barra horizontal comparativa ingresos vs gastos, con valores. */
@Composable
fun BarrasIngresoGasto(
    ingresosCentavos: Long,
    gastosCentavos: Long,
    colorIngreso: Color,
    colorGasto: Color,
    modifier: Modifier = Modifier,
) {
    val maximo = maxOf(ingresosCentavos, gastosCentavos, 1L)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BarraValor("Ingresos", ingresosCentavos, ingresosCentavos.toFloat() / maximo, colorIngreso)
        BarraValor("Gastos", gastosCentavos, gastosCentavos.toFloat() / maximo, colorGasto)
    }
}

@Composable
private fun BarraValor(etiqueta: String, valorCentavos: Long, fraccion: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
            Text(
                Dinero.formatear(valorCentavos),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(color),
            )
        }
    }
}
