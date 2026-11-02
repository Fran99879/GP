package com.tallerapp.core.backup

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Si la base de datos está sana o no, para toda la app.
 *
 * Mismo patrón que `NegocioActual` y `EstadoPlan`: un `StateFlow` global que las pantallas
 * observan. Lo revisa [com.tallerapp.TallerApplication] al arrancar, fuera del hilo principal.
 *
 * Avisar es todo lo que se puede hacer: una base corrupta no se repara sola, y borrarla por
 * nuestra cuenta sería tomar la decisión más destructiva posible en lugar del usuario. Lo que
 * corresponde es decírselo y dejarle a mano el camino de restaurar una copia.
 */
object EstadoBase {

    private val _danada = MutableStateFlow(false)

    /** True cuando la base no pasó el control de integridad al arrancar. */
    val danada: StateFlow<Boolean> = _danada.asStateFlow()

    /**
     * Lo llama el arranque de la app. Devuelve true si la base está sana.
     *
     * No hace falta limpiar el aviso después de restaurar: restaurar reinicia el proceso
     * (`reiniciarApp` en la pantalla de Configuración) y esto vuelve a correr de cero.
     */
    fun revisar(context: Context): Boolean {
        val sana = CopiaSeguridad.baseEstaSana(context)
        _danada.value = !sana
        return sana
    }
}
