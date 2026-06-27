package com.tallerapp.features.trabajos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.Trabajo

/**
 * Tarjeta de la lista de trabajos (Frozen Spec 7). Muestra la identificación
 * (patente o Marca+Modelo+Cliente - C5), estado actual y fecha de ingreso.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrabajoCard(
    trabajo: Trabajo,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = trabajo.identificacion(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${trabajo.marca} ${trabajo.modelo}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = trabajo.estadoReparacion.etiqueta,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = Fechas.formatear(trabajo.fechaIngreso),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
