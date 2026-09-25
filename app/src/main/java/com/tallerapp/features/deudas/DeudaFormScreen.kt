package com.tallerapp.features.deudas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.ui.components.CampoTexto
import com.tallerapp.core.ui.components.FechaPicker
import com.tallerapp.core.ui.components.PrimaryButton
import com.tallerapp.features.deudas.form.DeudaFormViewModel

/** Formulario de alta/edición de una deuda a favor ("quién te debe"). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeudaFormScreen(
    viewModel: DeudaFormViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val contactos by viewModel.contactos.collectAsStateWithLifecycle()

    LaunchedEffect(state.guardadoOk) {
        if (state.guardadoOk) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.titulo) },
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
            state.errores.general?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTexto(
                valor = state.nombre,
                onChange = viewModel::onNombreChange,
                etiqueta = "¿Quién te debe? *",
                error = state.errores.nombre,
            )
            // Sugerencias de contactos frecuentes (tocá para completar).
            val sugerencias = contactos
                .filter { state.nombre.isBlank() || it.contains(state.nombre, ignoreCase = true) }
                .filter { !it.equals(state.nombre, ignoreCase = true) }
                .take(8)
            if (sugerencias.isNotEmpty()) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    sugerencias.forEach { c ->
                        androidx.compose.material3.AssistChip(
                            onClick = { viewModel.onNombreChange(c) },
                            label = { Text(c) },
                        )
                    }
                }
            }
            CampoTexto(
                valor = state.monto,
                onChange = viewModel::onMontoChange,
                etiqueta = "Monto *",
                tipoTeclado = KeyboardType.Decimal,
                error = state.errores.monto,
            )
            CampoTexto(
                valor = state.nota,
                onChange = viewModel::onNotaChange,
                etiqueta = "Nota (opcional)",
                lineasMin = 2,
            )

            FechaPicker(fechaMillis = state.fecha, onFechaChange = viewModel::onFechaChange)

            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                androidx.compose.material3.Checkbox(
                    checked = state.fechaLimite != null,
                    onCheckedChange = { on -> viewModel.onFechaLimiteChange(if (on) com.tallerapp.core.util.Fechas.hoyInicioMillis() else null) },
                )
                Text("Fecha para cobrar (te avisamos si se pasa)")
            }
            if (state.fechaLimite != null) {
                FechaPicker(fechaMillis = state.fechaLimite!!, onFechaChange = { viewModel.onFechaLimiteChange(it) })
            }

            PrimaryButton("Guardar", viewModel::guardar, enabled = !state.procesando)
        }
    }
}
