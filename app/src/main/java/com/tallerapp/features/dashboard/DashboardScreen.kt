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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.ui.components.PlaceholderScaffold
import com.tallerapp.core.ui.components.PrimaryButton
import com.tallerapp.core.util.Dinero

/**
 * Pantalla inicial (Frozen Spec 12.1). Los 6 indicadores provienen de los módulos
 * de Trabajos y Finanzas (Fases 2–4), combinados en ObservarDashboardUseCase.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNuevoTrabajo: () -> Unit,
    onNuevoIngreso: () -> Unit,
    onNuevoGasto: () -> Unit,
    onVerTrabajos: () -> Unit,
    onVerFinanzas: () -> Unit,
    onVerReportes: () -> Unit,
) {
    val s by viewModel.state.collectAsStateWithLifecycle()

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

            Indicador("Caja del día", Dinero.formatear(s.cajaDelDiaCentavos))
            Indicador("Ganancia del mes", Dinero.formatear(s.gananciaDelMesCentavos))
            Indicador("Vehículos en el taller", s.vehiculosEnTaller.toString())
            Indicador("Esperando repuestos", s.esperandoRepuestos.toString())
            Indicador("Trabajos pendientes", s.pendientes.toString())
            Indicador("Entregados hoy", s.entregadosHoy.toString())

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
private fun Indicador(label: String, value: String) {
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
