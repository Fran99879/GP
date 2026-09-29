package com.tallerapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Producto del negocio: lo que se vende. Base del catálogo y de las facturas.
 *
 * El código de barras se guarda tal como lo devuelve el lector (EAN-13, UPC, QR interno…).
 * No es único a propósito: dos negocios pueden vender el mismo artículo y un escaneo
 * repetido no debe abortar el guardado. La búsqueda por código filtra además por negocio.
 *
 * `imagen` es la **ruta absoluta** de una copia propia del archivo en `filesDir/productos`,
 * no el Uri que devolvió el selector: esos Uri caducan al reiniciar el proceso.
 */
@Entity(
    tableName = "producto",
    // Los nombres deben coincidir con los índices de MIGRATION_13_14: Room valida
    // los índices al abrir la base y un nombre distinto aborta el arranque.
    indices = [
        Index(value = ["negocioId"], name = "ix_producto_negocio"),
        Index(value = ["codigoBarras"], name = "ix_producto_codigo"),
    ],
)
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val negocioId: Long,
    val nombre: String,
    val codigoBarras: String,
    val descripcion: String,
    /** Precio de lista, en centavos (AD-7). El descuento se aplica al mostrar y al facturar. */
    val precioCentavos: Long,
    /** Descuento del producto, en porcentaje (0 = sin descuento). */
    val descuentoPct: Double,
    /** Unidades en stock. Double porque hay rubros que venden por kilo o metro. */
    val stock: Double,
    /** Debajo de esta cantidad la lista avisa "stock bajo". 0 = sin aviso. */
    val stockMinimo: Double,
    /** Ruta absoluta de la foto, o "" si no tiene. */
    val imagen: String,
    val createdAt: Long,
)
