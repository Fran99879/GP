package com.tallerapp.domain.repository

import com.tallerapp.domain.model.Contacto
import kotlinx.coroutines.flow.Flow

/** Contrato de persistencia de clientes y proveedores. Filtra por el negocio activo. */
interface ContactoRepository {

    /** Contactos que sirven para ese tipo, filtrados por [busqueda] (vacía = todos). */
    fun observar(tipo: String, busqueda: String = ""): Flow<List<Contacto>>

    /** Solo los nombres, para el autocompletado que ya usaba el formulario de deudas. */
    fun observarNombres(): Flow<List<String>>

    suspend fun obtener(id: Long): Contacto?

    /** Contacto con ese nombre exacto en el negocio activo, o null. */
    suspend fun porNombre(nombre: String): Contacto?

    suspend fun crear(contacto: Contacto): Long
    suspend fun actualizar(contacto: Contacto)

    /** Borra el contacto y deja en NULL los vínculos; el historial no se toca. */
    suspend fun eliminar(id: Long)
}
