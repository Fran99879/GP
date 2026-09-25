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
) {
    val context = LocalContext.current

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

            // APLICACIÓN
            TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                EtiquetaGrupo("APLICACIÓN")
                TextoMuted("Base de datos: Local")
                TextoMuted("Modo: Offline-first")
            }
        }
    }
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
