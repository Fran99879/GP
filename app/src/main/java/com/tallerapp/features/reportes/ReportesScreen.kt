package com.tallerapp.features.reportes

import androidx.compose.runtime.Composable
import com.tallerapp.core.ui.components.PlaceholderBody
import com.tallerapp.core.ui.components.PlaceholderScaffold

/**
 * Reportes (Frozen Spec 12). Placeholder en Fase 1; los cálculos llegan en la Fase 6
 * y la exportación PDF/Excel en la Fase 7.
 */
@Composable
fun ReportesScreen(onBack: () -> Unit) {
    PlaceholderScaffold(title = "Reportes", onBack = onBack) { padding ->
        PlaceholderBody(
            padding = padding,
            message = "Reportes diario y mensual\n(funcionalidad en Fase 6 · exportación en Fase 7)",
        )
    }
}
