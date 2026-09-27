package com.tallerapp.core.billing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Plan activo del usuario, compartido por toda la app.
 *
 * Mismo patrón que [com.tallerapp.core.NegocioActual]: un `StateFlow` global que las
 * pantallas observan, así al activarse Pro la interfaz se actualiza sola sin reiniciar.
 *
 * Quien manda es Google Play: [FacturacionPlay] consulta las compras al iniciar y cada vez
 * que hay una, y escribe acá el resultado. No se persiste en preferencias a propósito —
 * guardarlo localmente sería trivial de falsear editando un archivo.
 */
object EstadoPlan {

    private val _plan = MutableStateFlow(Plan.GRATIS)
    val plan: StateFlow<Plan> = _plan.asStateFlow()

    val actual: Plan get() = _plan.value
    val esPro: Boolean get() = _plan.value.esPro

    /** Lo llama [FacturacionPlay] con lo que responde Google Play. */
    internal fun actualizar(nuevo: Plan) {
        _plan.value = nuevo
    }
}
