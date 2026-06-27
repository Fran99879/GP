package com.tallerapp.features.finanzas

import androidx.compose.runtime.Composable
import com.tallerapp.core.ui.components.PlaceholderBody
import com.tallerapp.core.ui.components.PlaceholderScaffold

/**
 * Alta de ingreso manual (Frozen Spec 9.5). Placeholder en Fase 1; campos y
 * validaciones (V-2, V-6) se implementan en la Fase 3.
 */
@Composable
fun IngresoFormScreen(onBack: () -> Unit) {
    PlaceholderScaffold(title = "Nuevo Ingreso", onBack = onBack) { padding ->
        PlaceholderBody(
            padding = padding,
            message = "Formulario de ingreso\n(funcionalidad en Fase 3)",
        )
    }
}
