package com.tallerapp.core.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tallerapp.core.util.Dinero

/** Modo de tema elegido por el usuario. */
enum class TemaModo { SISTEMA, CLARO, OSCURO }

/**
 * Preferencias de apariencia (tema claro/oscuro y moneda), persistidas en SharedPreferences.
 * Los estados observables de Compose (`modo`, `moneda`) recomponen la UI en vivo.
 */
object TemaApp {
    private const val PREFS = "mis_finanzas_prefs"
    private const val KEY_MODO = "tema_modo"
    private const val KEY_MONEDA = "moneda"
    private const val KEY_MONEDA_SEC = "moneda_sec"
    private const val KEY_TASA = "tasa"
    private const val KEY_EQUIV = "mostrar_equivalente"
    private const val KEY_ACCENT = "accent"
    private const val KEY_ONBOARDING = "onboarding_visto"
    private const val KEY_NOMBRE = "perfil_nombre"

    /** Monedas disponibles: (símbolo, etiqueta). */
    val monedas = listOf("$" to "Peso ($)", "US$" to "Dólar (US$)", "€" to "Euro (€)", "R$" to "Real (R$)", "Gs" to "Guaraní (Gs)")

    /** Colores de acento: (nombre, color ARGB). */
    val acentos = listOf(
        "Azul" to 0xFF023A5DL, "Rosa" to 0xFFC25E7AL, "Verde" to 0xFF3E8E6EL,
        "Violeta" to 0xFF7A5AA6L, "Rojo" to 0xFFC0544BL, "Gris" to 0xFF5F6B76L,
    )

    var modo by mutableStateOf(TemaModo.SISTEMA)
        private set
    var moneda by mutableStateOf("$")
        private set
    var accent by mutableStateOf("Azul")
        private set
    var onboardingVisto by mutableStateOf(false)
        private set
    var nombre by mutableStateOf("")
        private set

    /** Color de acento actual como ARGB Long. */
    val accentColor: Long get() = acentos.firstOrNull { it.first == accent }?.second ?: 0xFF023A5DL

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Carga las preferencias guardadas y las aplica. Llamar una vez al iniciar. */
    fun cargar(context: Context) {
        val p = prefs(context)
        modo = runCatching { TemaModo.valueOf(p.getString(KEY_MODO, "") ?: "") }.getOrDefault(TemaModo.SISTEMA)
        moneda = p.getString(KEY_MONEDA, "$") ?: "$"
        accent = p.getString(KEY_ACCENT, "Azul") ?: "Azul"
        onboardingVisto = p.getBoolean(KEY_ONBOARDING, false)
        nombre = p.getString(KEY_NOMBRE, "") ?: ""
        Dinero.simbolo = moneda
        Dinero.simboloSecundario = p.getString(KEY_MONEDA_SEC, "US$") ?: "US$"
        Dinero.tasa = p.getString(KEY_TASA, "0")?.toDoubleOrNull() ?: 0.0
        Dinero.mostrarEquivalente = p.getBoolean(KEY_EQUIV, false)
    }

    fun cambiar(context: Context, nuevo: TemaModo) {
        modo = nuevo
        prefs(context).edit().putString(KEY_MODO, nuevo.name).apply()
    }

    fun cambiarMoneda(context: Context, simbolo: String) {
        moneda = simbolo
        Dinero.simbolo = simbolo
        prefs(context).edit().putString(KEY_MONEDA, simbolo).apply()
    }

    fun cambiarAccent(context: Context, nombreAccent: String) {
        accent = nombreAccent
        prefs(context).edit().putString(KEY_ACCENT, nombreAccent).apply()
    }

    fun marcarOnboardingVisto(context: Context) {
        onboardingVisto = true
        prefs(context).edit().putBoolean(KEY_ONBOARDING, true).apply()
    }

    fun cambiarNombre(context: Context, nuevo: String) {
        nombre = nuevo
        prefs(context).edit().putString(KEY_NOMBRE, nuevo).apply()
    }

    fun cambiarConversion(context: Context, monedaSec: String, tasa: Double, mostrar: Boolean) {
        Dinero.simboloSecundario = monedaSec
        Dinero.tasa = if (tasa < 0) 0.0 else tasa
        Dinero.mostrarEquivalente = mostrar
        prefs(context).edit()
            .putString(KEY_MONEDA_SEC, monedaSec)
            .putString(KEY_TASA, tasa.toString())
            .putBoolean(KEY_EQUIV, mostrar)
            .apply()
    }

    val monedaSecundaria: String get() = Dinero.simboloSecundario
    val tasa: Double get() = Dinero.tasa
    val mostrarEquivalente: Boolean get() = Dinero.mostrarEquivalente
}
