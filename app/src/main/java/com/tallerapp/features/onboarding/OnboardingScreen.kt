package com.tallerapp.features.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tallerapp.core.ui.components.TarjetaApp
import com.tallerapp.core.ui.components.TextoMuted
import com.tallerapp.core.ui.components.TituloPantalla

private data class Tip(val titulo: String, val texto: String)

/** Mismos temas y textos guía que el onboarding del escritorio, adaptados al teléfono. */
private val TIPS = listOf(
    Tip(
        "💵  Cargá ingresos y gastos",
        "En «Movimientos» tocás + Ingreso o − Gasto. Elegís categoría y cuenta " +
            "(efectivo, banco, MercadoPago).",
    ),
    Tip(
        "🏷️  Personalizá categorías",
        "Desde Movimientos → Categorías podés crear las tuyas con ícono, y ponerles " +
            "un presupuesto mensual.",
    ),
    Tip(
        "🔁  Movimientos recurrentes",
        "Sueldo, alquiler o suscripciones se cargan solos cada mes. Configuralos en " +
            "Movimientos → Recurrentes.",
    ),
    Tip(
        "📊  Reportes y metas",
        "En «Reportes» ves gráficos, presupuestos y la evolución mes a mes. En «Inicio» " +
            "seguís tus metas de ahorro.",
    ),
    Tip(
        "🏪  Varios negocios",
        "Podés separar tus finanzas por negocio: cada uno guarda sus propios movimientos, " +
            "deudas y agenda. Cambiás de negocio desde el selector de arriba.",
    ),
    Tip(
        "⚙️  Tu info, segura y tuya",
        "Todo se guarda en tu teléfono. Desde Configuración cambiás el color, el modo " +
            "oscuro y la moneda.",
    ),
)

@Composable
fun OnboardingScreen(onEmpezar: () -> Unit) {
    // Surface propio: sin él la pantalla queda con el fondo blanco de la ventana
    // mientras el contenido usa los colores del tema (ilegible en modo oscuro).
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TituloPantalla("👋 ¡Bienvenido/a a Mis Finanzas!")
            TextoMuted("Una guía rápida para arrancar en 1 minuto.")

            TIPS.forEach { tip ->
                TarjetaApp(modifier = Modifier.fillMaxWidth()) {
                    Text(tip.titulo, fontWeight = FontWeight.SemiBold)
                    TextoMuted(tip.texto, modifier = Modifier.padding(top = 4.dp))
                }
            }

            Button(
                onClick = onEmpezar,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("¡Empezar!") }
        }
    }
}
