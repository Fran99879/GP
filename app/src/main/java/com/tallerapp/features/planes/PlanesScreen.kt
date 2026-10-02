package com.tallerapp.features.planes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.billing.EstadoPlan
import com.tallerapp.core.billing.FacturacionPlay
import com.tallerapp.core.billing.OfertaPro
import com.tallerapp.core.ui.components.BarraPlan
import com.tallerapp.core.ui.components.TarjetaApp
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.TituloPantalla
import com.tallerapp.core.ui.components.TituloSeccion
import com.tallerapp.core.ui.components.anchoContenido
import com.tallerapp.core.ui.theme.MetaCompleta

/** Funciones de cada plan, en el orden en que se muestran. */
private val GRATIS = listOf(
    "Un negocio, con movimientos ilimitados",
    "Ingresos y gastos por categoría y cuenta",
    "Quién te debe, con fecha límite",
    "Metas de ahorro y movimientos recurrentes",
    "Agenda de tareas, turnos y productos",
    "Reportes del mes con gráficos y presupuestos",
    "Calculadora, modo oscuro y varias monedas",
)

private val PRO = listOf(
    "Todo lo del plan gratuito",
    "Negocios ilimitados",
    "Comparativa entre negocios",
    "Ver todos los negocios combinados",
    "Catálogo de productos con código de barras",
    "Clientes y proveedores, con ficha y datos de contacto",
    "Facturas en PDF, con descuentos y control de stock",
    "Remitos en PDF",
    "Exportar a PDF y Excel",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanesScreen(facturacion: FacturacionPlay, onBack: () -> Unit) {
    val plan by EstadoPlan.plan.collectAsStateWithLifecycle()
    val ofertas by facturacion.ofertas.collectAsStateWithLifecycle()
    val pendiente by facturacion.compraPendiente.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planes") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Atrás") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .anchoContenido()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (plan.esPro) {
                TituloPantalla("Ya tenés Pro")
                TextoMuted(
                    "Gracias. Podés gestionar o cancelar la suscripción desde Google Play, " +
                        "en Pagos y suscripciones.",
                )
            } else {
                TituloPantalla("Pasá a Pro")
                TextoMuted("Uso personal gratis. Para tu negocio, Pro.")
            }

            // Los días que quedan del período, arriba de la comparativa.
            BarraPlan(onVerPlanes = {}, conAccion = false)

            if (pendiente) {
                TarjetaApp(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    conBorde = false,
                ) {
                    Text("Pago pendiente de confirmación", fontWeight = FontWeight.SemiBold)
                    TextoMuted(
                        "Google todavía no confirmó el pago. Apenas se acredite, Pro se " +
                            "activa solo.",
                    )
                }
            }

            TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                TituloSeccion("Gratis")
                TextoMuted("Para tus finanzas personales")
                GRATIS.forEach { Linea(it) }
            }

            TarjetaApp(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                conBorde = false,
            ) {
                TituloSeccion("Pro")
                TextoMuted("Para tu negocio")
                PRO.forEach { Linea(it) }

                if (plan.esPro) {
                    Text(
                        "Tenés Pro activo",
                        modifier = Modifier.padding(top = 12.dp),
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(
                        onClick = {
                            val uri = Uri.parse(
                                "https://play.google.com/store/account/subscriptions" +
                                    "?sku=${FacturacionPlay.PRODUCTO_PRO}" +
                                    "&package=${context.packageName}",
                            )
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Text("Gestionar suscripción en Google Play")
                    }
                } else {
                    if (ofertas.isEmpty()) {
                        TextoMuted(
                            "No se pudieron cargar los precios. Revisá tu conexión e " +
                                "intentá de nuevo.",
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    } else {
                        ofertas.forEach { oferta ->
                            BotonOferta(oferta) {
                                (context as? Activity)?.let { facturacion.comprar(it, oferta) }
                            }
                        }
                    }
                }
            }

            TextoMuted(
                "La suscripción se renueva sola y se puede cancelar cuando quieras desde " +
                    "Google Play.\n\nSi dejás de pagar no perdés nada: todos tus negocios y " +
                    "movimientos siguen visibles y los podés exportar.",
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun Linea(texto: String) {
    Row(
        modifier = Modifier.padding(top = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text("✓", color = MetaCompleta, fontWeight = FontWeight.Bold)
        Text(texto, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun BotonOferta(oferta: OfertaPro, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
    ) {
        Text(
            buildString {
                append("${oferta.etiqueta}: ${oferta.precio} ${oferta.periodo}")
                if (oferta.diasPrueba > 0) append("  ·  ${oferta.diasPrueba} días gratis")
            },
            fontWeight = FontWeight.SemiBold,
        )
    }
}
