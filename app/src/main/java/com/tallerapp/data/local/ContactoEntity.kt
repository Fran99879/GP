package com.tallerapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cliente o proveedor del negocio.
 *
 * Una sola tabla para los dos, con [tipo], porque en un kiosco o un taller el mismo
 * contacto te compra y te vende: con dos tablas habría que cargarlo dos veces y las
 * estadísticas nunca cerrarían. Las pantallas sí son dos, filtrando por tipo.
 *
 * El nombre es único **por negocio** (antes lo era globalmente, de cuando esto solo
 * autocompletaba deudas): dos negocios tienen que poder tener cada uno su "Juan".
 */
@Entity(
    tableName = "contacto",
    // El nombre debe coincidir con el índice de MIGRATION_14_15: Room valida los índices
    // al abrir la base y un nombre distinto aborta el arranque.
    indices = [
        Index(value = ["negocioId", "nombre"], name = "ix_contacto_negocio_nombre", unique = true),
    ],
)
data class ContactoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val negocioId: Long,
    val nombre: String,
    /** "cliente" | "proveedor" | "ambos". Ver `TipoContacto`. */
    val tipo: String,
    val documento: String,
    val telefono: String,
    val email: String,
    val direccion: String,
    val nota: String,
    val createdAt: Long,
)
