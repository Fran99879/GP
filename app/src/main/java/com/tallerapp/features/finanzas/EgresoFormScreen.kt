package com.tallerapp.features.finanzas

import androidx.compose.runtime.Composable
import com.tallerapp.core.ui.components.PlaceholderBody
import com.tallerapp.core.ui.components.PlaceholderScaffold

/**
 * Alta de egreso (Frozen Spec 9.6). Placeholder en Fase 1; campos y
 * validaciones (V-3) se implementan en la Fase 3.
 */
@Composable
fun EgresoFormScreen(onBack: () -> Unit) {
    PlaceholderScaffold(title = "Nuevo Gasto", onBack = onBack) { padding ->
        PlaceholderBody(
            padding = padding,
            message = "Formulario de gasto\n(funcionalidad en Fase 3)",
        )
    }
}
