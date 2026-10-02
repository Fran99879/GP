package com.tallerapp.features.finanzas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.tallerapp.core.ui.components.CampoMonto
import com.tallerapp.core.ui.components.FechaPicker
import com.tallerapp.core.ui.components.PrimaryButton
import com.tallerapp.core.ui.components.SelectorOpciones
import com.tallerapp.core.ui.components.anchoContenido
import com.tallerapp.features.finanzas.ingreso.IngresoFormViewModel

/** Formulario de alta/edición de ingreso (Frozen Spec 9.5, validaciones V-2/V-6). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngresoFormScreen(
    viewModel: IngresoFormViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
                .anchoContenido()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.errores.general?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoMonto(
                valor = state.monto,
                onChange = viewModel::onMontoChange,
                etiqueta = "Monto *",
                error = state.errores.monto,
                habilitado = state.editable,
            )
            CampoTexto(
                valor = state.concepto,
                onChange = viewModel::onConceptoChange,
                etiqueta = "Concepto *",
                error = state.errores.concepto,
                habilitado = state.editable,
            )
            SelectorOpciones(
                etiqueta = "Cuenta",
                seleccionado = state.cuentas.firstOrNull { it.nombre == state.cuenta },
                opciones = state.cuentas,
                textoOpcion = { it.display },
                onSeleccion = { viewModel.onCuentaChange(it.nombre) },
            )

            FechaPicker(fechaMillis = state.fecha, onFechaChange = viewModel::onFechaChange)

            PrimaryButton("Guardar", viewModel::guardar, enabled = !state.procesando)
        }
    }
}
