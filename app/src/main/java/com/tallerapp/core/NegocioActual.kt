package com.tallerapp.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Negocio actualmente seleccionado (compartido por toda la app). Las consultas filtran por
 * este id; al cambiarlo, las pantallas se actualizan (los usecases combinan este Flow).
 */
object NegocioActual {
    private const val PREFS = "mis_finanzas_prefs"
    private const val KEY = "negocio_actual_id"

    private val _id = MutableStateFlow(1L)
    val id: StateFlow<Long> = _id.asStateFlow()
    val value: Long get() = _id.value

    fun cargar(context: Context) {
        _id.value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY, 1L)
    }

    fun set(context: Context, negocioId: Long) {
        _id.value = negocioId
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putLong(KEY, negocioId).apply()
    }
}
