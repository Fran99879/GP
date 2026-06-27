package com.tallerapp.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tallerapp.core.ui.components.PlaceholderScaffold
import com.tallerapp.core.ui.components.PrimaryButton

/**
 * Pantalla inicial (Frozen Spec 12.1). En Fase 1 los indicadores son placeholders ("—");
 * sus valores reales se conectarán en la Fase 5 (Dashboard) desde los módulos previos.
 */
@Composable
fun DashboardScreen(
    onNuevoTrabajo: () -> Unit,
    onNuevoIngreso: () -> Unit,
    onNuevoGasto: () -> Unit,
    onVerTrabajos: () -> Unit,
    onVerFinanzas: () -> Unit,
    onVerReportes: () -> Unit,
) {
    PlaceholderScaffold(title = "TallerApp") { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Resumen", style = MaterialTheme.typography.titleLarge)

            // 6 indicadores del Dashboard (Frozen Spec 12.1) — sin datos en Fase 1.
            IndicatorPlaceholder("Caja del día", "—")
            IndicatorPlaceholder("Ganancia del mes", "—")
            IndicatorPlaceholder("Vehículos en el taller", "—")
            IndicatorPlaceholder("Esperando repuestos", "—")
            IndicatorPlaceholder("Trabajos pendientes", "—")
            IndicatorPlaceholder("Entregados hoy", "—")

            HorizontalDivider()

            Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium)
            PrimaryButton("Nuevo Trabajo", onNuevoTrabajo)
            PrimaryButton("Nuevo Ingreso", onNuevoIngreso)
            PrimaryButton("Nuevo Gasto", onNuevoGasto)

            HorizontalDivider()

            Text("Módulos", style = MaterialTheme.typography.titleMedium)
            PrimaryButton("Ver Trabajos", onVerTrabajos)
            PrimaryButton("Ver Finanzas", onVerFinanzas)
            PrimaryButton("Ver Reportes", onVerReportes)
        }
    }
}

@Composable
private fun IndicatorPlaceholder(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label)
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}
