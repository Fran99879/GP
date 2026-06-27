package com.tallerapp.features.reportes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.export.Compartir
import com.tallerapp.core.export.CsvExporter
import com.tallerapp.core.export.PdfExporter
import com.tallerapp.core.export.ReporteContenido
import com.tallerapp.core.ui.components.PlaceholderScaffold
import com.tallerapp.core.ui.components.PrimaryButton
import com.tallerapp.core.util.Dinero

/**
 * Reportes diario y mensual (Frozen Spec 12). Solo lectura; la exportación PDF/Excel
 * llega en la Fase 7. Un período sin movimientos muestra ceros (12.5).
 */
@Composable
fun ReportesScreen(
    viewModel: ReportesViewModel,
    onBack: () -> Unit,
) {
    val diario by viewModel.diario.collectAsStateWithLifecycle()
    val mensual by viewModel.mensual.collectAsStateWithLifecycle()
    val context = LocalContext.current

    PlaceholderScaffold(title = "Reportes", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Reporte del día", style = MaterialTheme.typography.titleLarge)
            Fila("Caja del día", Dinero.formatear(diario.ingresosCentavos))
            Fila("Ingresos del día", Dinero.formatear(diario.ingresosCentavos))
            Fila("Gastos del día", Dinero.formatear(diario.gastosCentavos))
            Fila("Ganancia del día", Dinero.formatear(diario.gananciaCentavos))

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Reporte del mes", style = MaterialTheme.typography.titleLarge)
            Fila("Ingresos del mes", Dinero.formatear(mensual.ingresosCentavos))
            Fila("Gastos del mes", Dinero.formatear(mensual.gastosCentavos))
            Fila("Ganancia del mes", Dinero.formatear(mensual.gananciaCentavos))
            Fila("Trabajos realizados", mensual.trabajosRealizados.toString())
            Fila("Vehículos atendidos", mensual.vehiculosAtendidos.toString())

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Servicios más realizados", style = MaterialTheme.typography.titleMedium)
            if (mensual.serviciosMasRealizados.isEmpty()) {
                Text("Sin datos este mes")
            } else {
                mensual.serviciosMasRealizados.forEach { conteo ->
                    Fila(conteo.servicio.etiqueta, conteo.cantidad.toString())
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            PrimaryButton("Exportar PDF") {
                val secciones = ReporteContenido.construir(diario, mensual)
                val uri = PdfExporter.generar(context, secciones)
                Compartir.archivo(context, uri, "application/pdf")
            }
            PrimaryButton("Exportar Excel") {
                val secciones = ReporteContenido.construir(diario, mensual)
                val uri = CsvExporter.generar(context, secciones)
                Compartir.archivo(context, uri, "text/csv")
            }
        }
    }
}

@Composable
private fun Fila(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(etiqueta)
        Text(valor, fontWeight = FontWeight.Bold)
    }
}
