package com.tallerapp.features.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.ui.components.TarjetaApp
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.TituloSeccion
import com.tallerapp.core.util.Fechas
import com.tallerapp.domain.model.AgendaItem
import com.tallerapp.domain.model.TipoAgenda
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Agenda mensual: calendario con marcas por día + lista del día seleccionado.
 * Equivale a la AgendaView del escritorio (tareas, turnos y productos).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(viewModel: AgendaViewModel, onOpenMenu: () -> Unit = {}, onBack: () -> Unit) {
    val mes by viewModel.mes.collectAsStateWithLifecycle()
    val dia by viewModel.diaSeleccionado.collectAsStateWithLifecycle()
    val items by viewModel.itemsDelMes.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<AgendaItem?>(null) }

    val delDia = items.filter { Fechas.aLocalDate(it.fecha) == dia }
    // Días del mes que tienen al menos una entrada (para el puntito del calendario).
    val diasConItems = items.map { Fechas.aLocalDate(it.fecha) }.toSet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda") },
                navigationIcon = { TextButton(onClick = onOpenMenu) { Text("☰") } },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editando = AgendaItem(titulo = "", fecha = Fechas.aMillis(dia))
            }) { Text("+", style = MaterialTheme.typography.headlineSmall) }
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
            // Selector de mes.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = viewModel::mesAnterior) {
                    Text("◀", style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    Fechas.etiquetaMes(mes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = viewModel::mesSiguiente) {
                    Text("▶", style = MaterialTheme.typography.titleLarge)
                }
            }

            TarjetaApp(modifier = Modifier.fillMaxWidth(), padding = 10.dp) {
                Calendario(
                    mes = mes,
                    diaSeleccionado = dia,
                    diasConItems = diasConItems,
                    onDiaClick = viewModel::seleccionarDia,
                )
            }

            TituloSeccion(Fechas.formatear(Fechas.aMillis(dia)))

            if (delDia.isEmpty()) {
                TextoMuted("Sin tareas, turnos ni productos para este día. Tocá + para agregar.")
            } else {
                delDia.forEach { item ->
                    FilaAgenda(
                        item = item,
                        onToggle = { viewModel.marcarHecho(item.id, !item.hecho) },
                        onClick = { editando = item },
                    )
                }
            }
        }
    }

    editando?.let { item ->
        DialogoAgenda(
            inicial = item,
            onCerrar = { editando = null },
            onGuardar = { viewModel.guardar(it); editando = null },
            onEliminar = if (item.id != 0L) {
                { viewModel.eliminar(item.id); editando = null }
            } else null,
        )
    }
}

/** Grilla mensual de 7 columnas, comenzando en lunes. */
@Composable
private fun Calendario(
    mes: java.time.YearMonth,
    diaSeleccionado: LocalDate,
    diasConItems: Set<LocalDate>,
    onDiaClick: (LocalDate) -> Unit,
) {
    val primerDia = mes.atDay(1)
    // Lunes = 1 … Domingo = 7; cuántas celdas vacías van antes del día 1.
    val offset = primerDia.dayOfWeek.value - DayOfWeek.MONDAY.value
    val totalDias = mes.lengthOfMonth()
    val celdas = offset + totalDias
    val filas = (celdas + 6) / 7
    val hoy = LocalDate.now()

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("L", "M", "M", "J", "V", "S", "D").forEach { d ->
                Text(
                    d,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        repeat(filas) { fila ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val indice = fila * 7 + col
                    val numero = indice - offset + 1
                    if (numero < 1 || numero > totalDias) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val fecha = mes.atDay(numero)
                        CeldaDia(
                            numero = numero,
                            seleccionado = fecha == diaSeleccionado,
                            esHoy = fecha == hoy,
                            tieneItems = fecha in diasConItems,
                            modifier = Modifier.weight(1f),
                            onClick = { onDiaClick(fecha) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CeldaDia(
    numero: Int,
    seleccionado: Boolean,
    esHoy: Boolean,
    tieneItems: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val fondo = if (seleccionado) MaterialTheme.colorScheme.primary else Color.Transparent
    val colorTexto = when {
        seleccionado -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(fondo)
            .then(
                if (esHoy && !seleccionado) {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                } else Modifier,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                numero.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = colorTexto,
                fontWeight = if (esHoy) FontWeight.Bold else FontWeight.Normal,
            )
            if (tieneItems) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            if (seleccionado) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary,
                        ),
                )
            }
        }
    }
}

@Composable
private fun FilaAgenda(item: AgendaItem, onToggle: () -> Unit, onClick: () -> Unit) {
    TarjetaApp(modifier = Modifier.fillMaxWidth(), padding = 10.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = item.hecho, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.weight(1f).clickable(onClick = onClick)) {
                Text(
                    "${TipoAgenda.icono(item.tipo)}  ${item.titulo}",
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (item.hecho) TextDecoration.LineThrough else null,
                )
                val detalle = listOfNotNull(
                    TipoAgenda.etiqueta(item.tipo),
                    item.hora.takeIf { it.isNotBlank() },
                    item.descripcion.takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                TextoMuted(detalle)
            }
        }
    }
}

@Composable
private fun DialogoAgenda(
    inicial: AgendaItem,
    onCerrar: () -> Unit,
    onGuardar: (AgendaItem) -> Unit,
    onEliminar: (() -> Unit)?,
) {
    var titulo by remember { mutableStateOf(inicial.titulo) }
    var descripcion by remember { mutableStateOf(inicial.descripcion) }
    var hora by remember { mutableStateOf(inicial.hora) }
    var tipo by remember { mutableStateOf(inicial.tipo) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (inicial.id == 0L) "Nueva entrada" else "Editar entrada") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoAgenda.todos.forEach { t ->
                        FilterChip(
                            selected = tipo == t,
                            onClick = { tipo = t },
                            label = { Text(TipoAgenda.etiqueta(t)) },
                        )
                    }
                }
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = hora,
                    onValueChange = { hora = it },
                    label = { Text("Hora (opcional, ej. 14:30)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = titulo.isNotBlank(),
                onClick = {
                    onGuardar(
                        inicial.copy(
                            tipo = tipo,
                            titulo = titulo.trim(),
                            descripcion = descripcion.trim(),
                            hora = hora.trim(),
                        ),
                    )
                },
            ) { Text("Guardar") }
        },
        dismissButton = {
            Row {
                if (onEliminar != null) {
                    TextButton(onClick = onEliminar) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onCerrar) { Text("Cancelar") }
            }
        },
    )
}
