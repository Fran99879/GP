package com.tallerapp.data.local

/**
 * Datos con los que arranca una instalación nueva.
 *
 * Los mismos valores están sembrados en [MIGRATION_4_5] y [MIGRATION_5_6], pero las
 * migraciones solo corren al **actualizar**: una instalación nueva crea las tablas desde el
 * esquema de las entidades y no ejecuta ninguna. Sin esto el usuario abría la app sin
 * categorías ni cuentas. Si se tocan estas listas, tocar también esas migraciones para que
 * el que actualiza y el que instala de cero vean lo mismo.
 */
object DatosIniciales {

    val CATEGORIAS = listOf(
        CategoriaEntity(tipo = "egreso", nombre = "Alimentos", color = "#4A7C59", icono = "🍽️", orden = 1, activo = true),
        CategoriaEntity(tipo = "egreso", nombre = "Transporte", color = "#0A6E8C", icono = "🚗", orden = 2, activo = true),
        CategoriaEntity(tipo = "egreso", nombre = "Servicios", color = "#023A5D", icono = "🧾", orden = 3, activo = true),
        CategoriaEntity(tipo = "egreso", nombre = "Hogar", color = "#98643A", icono = "🏠", orden = 4, activo = true),
        CategoriaEntity(tipo = "egreso", nombre = "Salud", color = "#C0544B", icono = "⚕️", orden = 5, activo = true),
        CategoriaEntity(tipo = "egreso", nombre = "Ocio", color = "#7A5AA6", icono = "🎉", orden = 6, activo = true),
        CategoriaEntity(tipo = "egreso", nombre = "Otros", color = "#8A8D91", icono = "📦", orden = 7, activo = true),
        CategoriaEntity(tipo = "ingreso", nombre = "Sueldo", color = "#3E8E6E", icono = "💼", orden = 1, activo = true),
        CategoriaEntity(tipo = "ingreso", nombre = "Ventas", color = "#0A6E8C", icono = "🛒", orden = 2, activo = true),
        CategoriaEntity(tipo = "ingreso", nombre = "Extras", color = "#C99A6D", icono = "✨", orden = 3, activo = true),
        CategoriaEntity(tipo = "ingreso", nombre = "Otros", color = "#8A8D91", icono = "📦", orden = 4, activo = true),
    )

    // "Efectivo" no es opcional: es el valor por defecto de la columna `cuenta` de ingreso y
    // egreso, y CuentaRepositoryImpl.eliminar() se niega a borrarla.
    val CUENTAS = listOf(
        CuentaEntity(nombre = "Efectivo", icono = "💵", saldoInicialCentavos = 0, orden = 1, activo = true),
        CuentaEntity(nombre = "Banco", icono = "🏦", saldoInicialCentavos = 0, orden = 2, activo = true),
        CuentaEntity(nombre = "MercadoPago", icono = "💳", saldoInicialCentavos = 0, orden = 3, activo = true),
    )
}
