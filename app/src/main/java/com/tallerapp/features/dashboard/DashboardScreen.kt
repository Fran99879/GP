package com.tallerapp.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.ui.components.PrimaryButton
import com.tallerapp.core.ui.theme.Deuda
import com.tallerapp.core.ui.theme.Gasto
import com.tallerapp.core.ui.theme.Ingreso
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas

/** Pantalla inicial: balance del mes, indicadores rápidos y accesos a los módulos. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNuevoIngreso: () -> Unit,
    onNuevoGasto: () -> Unit,
    onVerFinanzas: () -> Unit,
    onVerDeudas: () -> Unit,
    onVerReportes: () -> Unit,
) {
    val s by viewModel.state.collectAsStateWithLifecycle()
    val mes = Fechas.etiquetaMes(Fechas.mesActual())

    androidx.compose.material3.Scaffold(
        topBar = { TopAppBar(title = { Text("Mis Finanzas") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Balance del mes (hero).
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Balance de $mes", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        Dinero.formatear(s.balanceDelMesCentavos),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ingresos", style = MaterialTheme.typography.labelMedium)
                            Text(
                                Dinero.formatear(s.ingresosDelMesCentavos),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gastos", style = MaterialTheme.typography.labelMedium)
                            Text(
                                Dinero.formatear(s.gastosDelMesCentavos),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            // Indicadores secundarios.
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    titulo = "Ingresos hoy",
                    valor = Dinero.formatear(s.cajaDelDiaCentavos),
                    color = Ingreso,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    titulo = if (s.cantidadDeudasPendientes == 1) "Te debe 1 persona" else "Te deben ${s.cantidadDeudasPendientes}",
                    valor = Dinero.formatear(s.deudasPendientesCentavos),
                    color = Deuda,
                    modifier = Modifier.weight(1f),
                )
            }

            Text("Registrar", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton("+ Ingreso", onNuevoIngreso, modifier = Modifier.weight(1f))
                PrimaryButton("− Gasto", onNuevoGasto, modifier = Modifier.weight(1f))
            }

            Text("Ver", style = MaterialTheme.typography.titleMedium)
            PrimaryButton("Movimientos del día", onVerFinanzas)
            PrimaryButton("Quién te debe", onVerDeudas)
            PrimaryButton("Reportes y gráficos", onVerReportes)
        }
    }
}

@Composable
private fun StatCard(
    titulo: String,
    valor: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                titulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text(valor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
