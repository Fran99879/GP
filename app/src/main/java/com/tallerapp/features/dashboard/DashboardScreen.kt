package com.tallerapp.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.ui.components.BotonGhost
import com.tallerapp.core.ui.components.FilaMeta
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.TarjetaMetrica
import com.tallerapp.core.ui.components.TituloPantalla
import com.tallerapp.core.ui.components.TituloSeccion
import com.tallerapp.core.ui.theme.Deuda
import com.tallerapp.core.ui.theme.Gasto
import com.tallerapp.core.ui.theme.Ingreso
import com.tallerapp.core.util.Dinero
import com.tallerapp.core.util.Fechas
import com.tallerapp.features.negocios.SelectorNegocioTopBar

/**
 * Pantalla inicial. Replica el Dashboard del escritorio: 4 tarjetas de resumen
 * (Ingresos, Gastos, Balance destacado y Me deben), recordatorio de deudas y accesos rápidos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenMenu: () -> Unit = {},
    onNuevoIngreso: () -> Unit,
    onNuevoGasto: () -> Unit,
    onVerFinanzas: () -> Unit,
    onVerDeudas: () -> Unit,
    onVerReportes: () -> Unit,
    onVerAjustes: () -> Unit = {},
    onVerPerfil: () -> Unit = {},
    onVerNegocios: () -> Unit = {},
    onVerCalculadora: () -> Unit = {},
    onVerMetas: () -> Unit = {},
    onVerAgenda: () -> Unit = {},
) {
    val s by viewModel.state.collectAsStateWithLifecycle()
    val metas by viewModel.metas.collectAsStateWithLifecycle()
    val mes = Fechas.etiquetaMes(Fechas.mesActual())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Finanzas") },
                navigationIcon = { TextButton(onClick = onOpenMenu) { Text("☰") } },
                actions = {
                    SelectorNegocioTopBar()
                    IconButton(onClick = onVerPerfil) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Text("👤") }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TituloPantalla("Resumen de $mes")

            // Tarjetas de resumen (en el escritorio son 4 en fila; acá 2×2).
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaMetrica(
                    etiqueta = "Ingresos",
                    valor = Dinero.formatear(s.ingresosDelMesCentavos),
                    colorValor = Ingreso,
                    modifier = Modifier.weight(1f),
                )
                TarjetaMetrica(
                    etiqueta = "Gastos",
                    valor = Dinero.formatear(s.gastosDelMesCentavos),
                    colorValor = Gasto,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaMetrica(
                    etiqueta = "Balance",
                    valor = Dinero.formatear(s.balanceDelMesCentavos),
                    subtitulo = Dinero.equivalente(s.balanceDelMesCentavos).ifEmpty { null },
                    destacada = true,
                    modifier = Modifier.weight(1f),
                )
                TarjetaMetrica(
                    etiqueta = "Me deben",
                    valor = Dinero.formatear(s.deudasPendientesCentavos),
                    colorValor = Deuda,
                    modifier = Modifier.weight(1f),
                )
            }

            // Recordatorio de deudas pendientes (banner del escritorio).
            if (s.cantidadDeudasPendientes > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gasto.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    val cuantos = s.cantidadDeudasPendientes
                    Text(
                        if (cuantos == 1) "Tenés 1 deuda pendiente de cobro."
                        else "Tenés $cuantos deudas pendientes de cobro.",
                        color = Gasto,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }


            // Metas de ahorro (igual que el Dashboard del escritorio).
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                TituloSeccion("Metas de ahorro", modifier = Modifier.weight(1f))
                TextButton(onClick = onVerMetas) { Text("Gestionar") }
            }
            if (metas.isEmpty()) {
                TextoMuted(
                    "Fijá una meta (ej. 'Vacaciones $300.000') y seguí tu avance. " +
                        "Tocá 'Gestionar' para empezar.",
                )
            } else {
                metas.forEach { m ->
                    FilaMeta(
                        nombre = m.nombre,
                        textoMontos = "${Dinero.formatear(m.actualCentavos)} / ${Dinero.formatear(m.objetivoCentavos)}",
                        fraccion = m.fraccion,
                    )
                }
            }
            TituloSeccion("Registrar", modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonGhost("＋  Ingreso", onNuevoIngreso, modifier = Modifier.weight(1f))
                BotonGhost("－  Gasto", onNuevoGasto, modifier = Modifier.weight(1f))
            }

            TituloSeccion("Accesos rápidos", modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonGhost("💵  Movimientos", onVerFinanzas, modifier = Modifier.weight(1f))
                BotonGhost("👥  Me deben", onVerDeudas, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonGhost("📊  Reportes", onVerReportes, modifier = Modifier.weight(1f))
                BotonGhost("🏪  Negocios", onVerNegocios, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonGhost("📅  Agenda", onVerAgenda, modifier = Modifier.weight(1f))
                BotonGhost("🧮  Calculadora", onVerCalculadora, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonGhost("⚙️  Configuración", onVerAjustes, modifier = Modifier.weight(1f))
            }
        }
    }
}
