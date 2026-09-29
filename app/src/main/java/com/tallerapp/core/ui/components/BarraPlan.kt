package com.tallerapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.billing.EstadoPlan
import com.tallerapp.core.billing.EstadoSuscripcion
import com.tallerapp.core.billing.Suscripcion
import com.tallerapp.core.ui.theme.Gasto
import com.tallerapp.core.util.Fechas

/**
 * Barra con los días que le quedan al plan.
 *
 * En prueba y en Pro muestra el avance del período; en Gratis, una línea con el enlace a
 * Planes. La cuenta de días es una estimación del lado de la app: ver [Suscripcion].
 */
@Composable
fun BarraPlan(
    onVerPlanes: () -> Unit,
    modifier: Modifier = Modifier,
    /** En la propia pantalla de Planes no tiene sentido ofrecer ir a Planes. */
    conAccion: Boolean = true,
) {
    val plan by EstadoPlan.plan.collectAsStateWithLifecycle()
    val sub by EstadoPlan.suscripcion.collectAsStateWithLifecycle()

    // Sin período que mostrar: plan Gratis, o Pro sin datos de compra todavía.
    if (!sub.tienePeriodo) {
        TarjetaApp(modifier = modifier.fillMaxWidth(), padding = 12.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (plan.esPro) "Pro activo" else "Plan Gratis",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (!plan.esPro && conAccion) {
                    TextButton(onClick = onVerPlanes) { Text("Ver Pro") }
                }
            }
            if (!plan.esPro) {
                TextoMuted("Uso personal completo. Para tu negocio, Pro.")
            }
        }
        return
    }

    val urgente = sub.porTerminar || sub.estado == EstadoSuscripcion.CANCELADA
    val color = if (urgente) Gasto else MaterialTheme.colorScheme.primary
    val titulo = when (sub.estado) {
        EstadoSuscripcion.PRUEBA -> "Prueba de Pro"
        EstadoSuscripcion.CANCELADA -> "Pro hasta el ${Fechas.formatear(sub.finMillis)}"
        else -> "Pro activo"
    }
    val dias = if (sub.diasRestantes == 1) "1 día" else "${sub.diasRestantes} días"
    val detalle = when (sub.estado) {
        EstadoSuscripcion.PRUEBA ->
            "Quedan $dias de ${sub.diasTotales} de prueba · termina el ${Fechas.formatear(sub.finMillis)}"
        EstadoSuscripcion.CANCELADA ->
            "Vence en $dias. Después vuelve al plan Gratis."
        else -> "Se renueva en $dias · ${Fechas.formatear(sub.finMillis)}"
    }

    TarjetaApp(modifier = modifier.fillMaxWidth(), padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                titulo,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                dias,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
            )
        }
        BarraProgreso(fraccion = sub.fraccionRestante, color = color)
        TextoMuted(detalle)
        if (conAccion && sub.estado != EstadoSuscripcion.ACTIVA) {
            TextButton(
                onClick = onVerPlanes,
                modifier = Modifier.padding(top = 2.dp),
            ) {
                Text(if (sub.estado == EstadoSuscripcion.PRUEBA) "Ver planes" else "Renovar Pro")
            }
        }
    }
}

/** Barra de 10dp con el mismo trazo que la de las metas de ahorro. */
@Composable
private fun BarraProgreso(fraccion: Float, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.outline),
    ) {
        val llena = fraccion.coerceIn(0f, 1f)
        if (llena > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(llena.coerceAtLeast(0.0001f))
                    .background(color),
            )
        }
        if (llena < 1f) {
            Box(modifier = Modifier.weight((1f - llena).coerceAtLeast(0.0001f)))
        }
    }
}
