package com.tallerapp.features.trabajos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tallerapp.core.ui.components.PrimaryButton
import com.tallerapp.domain.model.ServicioRealizado
import com.tallerapp.features.trabajos.form.TrabajoFormViewModel

/**
 * Formulario de alta/edición de trabajo (Frozen Spec 9.1, validaciones V-1/V-4/V-8).
 * Al guardar con éxito navega hacia atrás.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrabajoFormScreen(
    viewModel: TrabajoFormViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.guardadoId) {
        if (state.guardadoId != null) onBack()
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
            CampoTexto(
                valor = state.cliente,
                onChange = viewModel::onClienteChange,
                etiqueta = "Cliente *",
                error = state.errores.cliente,
            )
            CampoTexto(
                valor = state.telefono,
                onChange = viewModel::onTelefonoChange,
                etiqueta = "Teléfono",
                tipoTeclado = KeyboardType.Phone,
            )
            CampoTexto(
                valor = state.patente,
                onChange = viewModel::onPatenteChange,
                etiqueta = "Patente",
            )
            CampoTexto(
                valor = state.marca,
                onChange = viewModel::onMarcaChange,
                etiqueta = "Marca *",
                error = state.errores.marca,
            )
            CampoTexto(
                valor = state.modelo,
                onChange = viewModel::onModeloChange,
                etiqueta = "Modelo *",
                error = state.errores.modelo,
            )

            SelectorServicio(
                seleccionado = state.servicio,
                onSeleccion = viewModel::onServicioChange,
                error = state.errores.servicio,
            )

            CampoTexto(
                valor = state.precio,
                onChange = viewModel::onPrecioChange,
                etiqueta = "Precio del trabajo *",
                tipoTeclado = KeyboardType.Decimal,
                error = state.errores.precio,
                habilitado = state.precioEditable,
            )

            CampoTexto(
                valor = state.problema,
                onChange = viewModel::onProblemaChange,
                etiqueta = "Problema informado",
                lineasMin = 2,
            )
            CampoTexto(
                valor = state.diagnostico,
                onChange = viewModel::onDiagnosticoChange,
                etiqueta = "Diagnóstico",
                lineasMin = 2,
            )

            PrimaryButton("Guardar", viewModel::guardar)
        }
    }
}

@Composable
private fun CampoTexto(
    valor: String,
    onChange: (String) -> Unit,
    etiqueta: String,
    error: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    habilitado: Boolean = true,
    lineasMin: Int = 1,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(etiqueta) },
        isError = error != null,
        enabled = habilitado,
        singleLine = lineasMin == 1,
        minLines = lineasMin,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        supportingText = error?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorServicio(
    seleccionado: ServicioRealizado?,
    onSeleccion: (ServicioRealizado) -> Unit,
    error: String?,
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
    ) {
        OutlinedTextField(
            value = seleccionado?.etiqueta ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Servicio realizado *") },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false },
        ) {
            ServicioRealizado.entries.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion.etiqueta) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    },
                )
            }
        }
    }
}
