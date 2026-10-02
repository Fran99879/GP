package com.tallerapp.features.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tallerapp.core.ui.components.SelectorOpciones
import com.tallerapp.core.ui.components.TarjetaApp
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.TituloPantalla
import com.tallerapp.core.billing.EstadoPlan
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.content.Intent
import androidx.compose.material3.AlertDialog
import com.tallerapp.core.backup.CopiaSeguridad
import com.tallerapp.core.ui.components.anchoContenido
import com.tallerapp.core.ui.theme.TemaApp
import com.tallerapp.core.ui.theme.TemaModo

/**
 * Configuración. Sigue la estructura del escritorio: tarjetas por grupo con el
 * título del grupo en MAYÚSCULAS y estilo `Muted` (APARIENCIA, MONEDA, HERRAMIENTAS…).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    onBack: () -> Unit,
    onVerCategorias: () -> Unit = {},
    onVerCuentas: () -> Unit = {},
    onVerMetas: () -> Unit = {},
    onVerRecurrentes: () -> Unit = {},
    onVerNegocios: () -> Unit = {},
    onVerAgenda: () -> Unit = {},
    onVerProductos: () -> Unit = {},
    onVerFacturas: () -> Unit = {},
    onVerPlanes: () -> Unit = {},
) {
    val context = LocalContext.current
    val plan by EstadoPlan.plan.collectAsStateWithLifecycle()
    val esPro = plan.esPro

    var avisoCopia by remember { mutableStateOf<String?>(null) }
    var confirmarRestaurar by remember { mutableStateOf(false) }

    // Selector del sistema: el usuario elige dónde guardar y la app no pide permisos.
    val exportar = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        if (uri != null) {
            avisoCopia = when (val r = CopiaSeguridad.exportar(context, uri)) {
                is CopiaSeguridad.Resultado.Exito -> r.mensaje
                is CopiaSeguridad.Resultado.Error -> r.mensaje
            }
        }
    }

    val importar = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            when (val r = CopiaSeguridad.restaurar(context, uri)) {
                is CopiaSeguridad.Resultado.Exito -> reiniciarApp(context)
                is CopiaSeguridad.Resultado.Error -> avisoCopia = r.mensaje
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
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
            TituloPantalla("Configuración")

            // APARIENCIA
            TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                EtiquetaGrupo("APARIENCIA")
                Text("Modo", modifier = Modifier.padding(bottom = 4.dp))
                OpcionTema("Seguir al sistema", TemaModo.SISTEMA, context)
                OpcionTema("Claro", TemaModo.CLARO, context)
                OpcionTema("Oscuro", TemaModo.OSCURO, context)
                Text("Color de la aplicación", modifier = Modifier.padding(top = 10.dp, bottom = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TemaApp.acentos.forEach { (nombre, color) ->
                        val sel = TemaApp.accent == nombre
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color(color), CircleShape)
                                .border(
                                    if (sel) 3.dp else 0.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    CircleShape,
                                )
                                .clickable { TemaApp.cambiarAccent(context, nombre) },
                        )
                    }
                }
            }

            // MONEDA
            TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                EtiquetaGrupo("MONEDA")
                ContenidoMoneda(context)
            }

            // HERRAMIENTAS
            TarjetaApp(modifier = Modifier.fillMaxWidth(), padding = 0.dp) {
                EtiquetaGrupo("HERRAMIENTAS", modifier = Modifier.padding(start = 16.dp, top = 16.dp))
                FilaHerramienta("🏪", "Negocios", onVerNegocios)
                HorizontalDivider()
                // Con plan Gratis quedan con candado y llevan a Planes, igual que en el menú.
                FilaHerramienta(
                    if (esPro) "📦" else "🔒", "Productos",
                    if (esPro) onVerProductos else onVerPlanes,
                )
                HorizontalDivider()
                FilaHerramienta(
                    if (esPro) "🧾" else "🔒", "Facturas",
                    if (esPro) onVerFacturas else onVerPlanes,
                )
                HorizontalDivider()
                FilaHerramienta("📅", "Agenda", onVerAgenda)
                HorizontalDivider()
                FilaHerramienta("🏷️", "Categorías", onVerCategorias)
                HorizontalDivider()
                FilaHerramienta("🏦", "Cuentas / medios de pago", onVerCuentas)
                HorizontalDivider()
                FilaHerramienta("🎯", "Metas de ahorro", onVerMetas)
                HorizontalDivider()
                FilaHerramienta("🔁", "Movimientos recurrentes", onVerRecurrentes)
            }

            // COPIA DE SEGURIDAD
            TarjetaApp(modifier = Modifier.fillMaxWidth(), padding = 0.dp) {
                EtiquetaGrupo(
                    "COPIA DE SEGURIDAD",
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                )
                FilaHerramienta("💾", "Guardar una copia", { exportar.launch(CopiaSeguridad.nombreSugerido()) })
                HorizontalDivider()
                FilaHerramienta("♻️", "Restaurar una copia", { confirmarRestaurar = true })
                TextoMuted(
                    "Los datos viven solo en este teléfono: si desinstalás la app, se borran. " +
                        "Guardá la copia donde quieras (Drive, WhatsApp, la tarjeta SD) y usala " +
                        "para pasarlos a otro teléfono. Las fotos de los productos no entran en la copia.",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                )
            }

            // APLICACIÓN
            TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                EtiquetaGrupo("APLICACIÓN")
                TextoMuted("Base de datos: Local")
                TextoMuted("Modo: Offline-first")
            }
        }
    }

    // Restaurar pisa todo: se avisa antes de abrir el selector, no después.
    if (confirmarRestaurar) {
        AlertDialog(
            onDismissRequest = { confirmarRestaurar = false },
            title = { Text("Restaurar una copia") },
            text = {
                Text(
                    "Los datos que tenés ahora en el teléfono se reemplazan por los de la copia. " +
                        "Esto no se puede deshacer. " +
                        "Si querés conservarlos, guardá una copia primero.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarRestaurar = false
                        importar.launch(arrayOf("application/zip", "application/octet-stream"))
                    },
                ) { Text("Elegir archivo") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarRestaurar = false }) { Text("Cancelar") }
            },
        )
    }

    avisoCopia?.let { mensaje ->
        AlertDialog(
            onDismissRequest = { avisoCopia = null },
            title = { Text("Copia de seguridad") },
            text = { Text(mensaje) },
            confirmButton = { TextButton(onClick = { avisoCopia = null }) { Text("Entendido") } },
        )
    }
}

/**
 * Reinicia la app después de restaurar. Room mantiene en memoria la base que se acaba de
 * reemplazar, y las pantallas siguen mostrando los datos viejos hasta que el proceso arranca
 * de nuevo; con los datos del usuario en juego, es preferible el reinicio a un estado a medias.
 */
private fun reiniciarApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    Runtime.getRuntime().exit(0)
}

/** Título de grupo en mayúsculas (estilo `Muted` del escritorio). */
@Composable
private fun EtiquetaGrupo(texto: String, modifier: Modifier = Modifier) {
    TextoMuted(texto, modifier = modifier.padding(bottom = 10.dp))
}

@Composable
private fun FilaHerramienta(icono: String, texto: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icono, modifier = Modifier.padding(end = 12.dp))
        Text(texto)
    }
}

@Composable
private fun ContenidoMoneda(context: android.content.Context) {
    var equiv by remember { mutableStateOf(TemaApp.mostrarEquivalente) }
    var sec by remember { mutableStateOf(TemaApp.monedaSecundaria) }
    var tasa by remember { mutableStateOf(if (TemaApp.tasa > 0) TemaApp.tasa.toString() else "") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SelectorOpciones(
            etiqueta = "Moneda",
            seleccionado = TemaApp.monedas.firstOrNull { it.first == TemaApp.moneda },
            opciones = TemaApp.monedas,
            textoOpcion = { it.second },
            onSeleccion = { TemaApp.cambiarMoneda(context, it.first) },
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = equiv,
                onCheckedChange = {
                    equiv = it
                    TemaApp.cambiarConversion(context, sec, tasa.toDoubleOrNull() ?: 0.0, it)
                },
            )
            Text("Mostrar equivalente en otra moneda")
        }
        if (equiv) {
            SelectorOpciones(
                etiqueta = "Moneda secundaria",
                seleccionado = TemaApp.monedas.firstOrNull { it.first == sec },
                opciones = TemaApp.monedas,
                textoOpcion = { it.second },
                onSeleccion = {
                    sec = it.first
                    TemaApp.cambiarConversion(context, sec, tasa.toDoubleOrNull() ?: 0.0, true)
                },
            )
            OutlinedTextField(
                value = tasa,
                onValueChange = {
                    tasa = it
                    TemaApp.cambiarConversion(context, sec, it.toDoubleOrNull() ?: 0.0, true)
                },
                label = { Text("1 $sec = ? ${TemaApp.moneda}") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
        }
    }
}

@Composable
private fun OpcionTema(texto: String, valor: TemaModo, context: android.content.Context) {
    val seleccionado = TemaApp.modo == valor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = seleccionado, onClick = { TemaApp.cambiar(context, valor) })
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = seleccionado, onClick = { TemaApp.cambiar(context, valor) })
        Text(texto, modifier = Modifier.padding(start = 12.dp))
    }
}
