package com.tallerapp.features.finanzas

import androidx.compose.runtime.Composable
import com.tallerapp.core.ui.components.PlaceholderBody
import com.tallerapp.core.ui.components.PlaceholderScaffold
import com.tallerapp.core.ui.components.PrimaryButton

/**
 * Hub de Finanzas (Frozen Spec 8). En Fase 1 solo enlaza a las altas de ingreso/egreso.
 * Listados, caja y edición del día se implementan en la Fase 3.
 */
@Composable
fun FinanzasScreen(
    onBack: () -> Unit,
    onNuevoIngreso: () -> Unit,
    onNuevoGasto: () -> Unit,
) {
    PlaceholderScaffold(title = "Finanzas", onBack = onBack) { padding ->
        PlaceholderBody(
            padding = padding,
            message = "Ingresos y Egresos\n(funcionalidad en Fase 3)",
        ) {
            PrimaryButton("Nuevo Ingreso", onNuevoIngreso)
            PrimaryButton("Nuevo Gasto", onNuevoGasto)
        }
    }
}
