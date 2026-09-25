package com.tallerapp.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migraciones de la base que preservan los datos entre versiones de esquema.
 * Reemplazan a fallbackToDestructiveMigration (acción D-4 de la auditoría final).
 * El SQL replica exactamente lo que Room genera para cada entidad.
 */

/** v1 → v2: se agregan las tablas de Finanzas (ingreso y egreso) — Fase 3. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `ingreso` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `montoCentavos` INTEGER NOT NULL,
                `concepto` TEXT NOT NULL,
                `metodo` TEXT NOT NULL,
                `repEfectivo` INTEGER,
                `repTransferencia` INTEGER,
                `repTarjeta` INTEGER,
                `repMercadoPago` INTEGER,
                `fecha` INTEGER NOT NULL,
                `fechaRegistro` INTEGER NOT NULL,
                `origen` TEXT NOT NULL,
                `trabajoId` INTEGER
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ingreso_fecha` ON `ingreso` (`fecha`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `egreso` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `montoCentavos` INTEGER NOT NULL,
                `categoria` TEXT NOT NULL,
                `concepto` TEXT NOT NULL,
                `fecha` INTEGER NOT NULL,
                `fechaRegistro` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_egreso_fecha` ON `egreso` (`fecha`)")
    }
}

/** v2 → v3: se agrega fechaEntrega al trabajo (para "Entregados hoy") — Fase 5. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `trabajo` ADD COLUMN `fechaEntrega` INTEGER")
    }
}

/**
 * v3 → v4: la app deja de ser de taller. Se retira la tabla de trabajos y se agrega
 * la de deudas a favor ("quién te debe"). Ingresos y egresos se conservan intactos.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `trabajo`")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `deuda` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `nombre` TEXT NOT NULL,
                `montoCentavos` INTEGER NOT NULL,
                `fecha` INTEGER NOT NULL,
                `nota` TEXT NOT NULL,
                `cobrada` INTEGER NOT NULL,
                `fechaCobro` INTEGER,
                `fechaRegistro` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_deuda_cobrada` ON `deuda` (`cobrada`)")

        // Remapea las categorías de gasto del taller a las de finanzas personales.
        db.execSQL("UPDATE `egreso` SET `categoria` = 'TRANSPORTE' WHERE `categoria` = 'COMBUSTIBLE'")
        db.execSQL("UPDATE `egreso` SET `categoria` = 'SERVICIOS' WHERE `categoria` = 'SERVICIOS_E_IMPUESTOS'")
        db.execSQL("UPDATE `egreso` SET `categoria` = 'OTROS' WHERE `categoria` IN ('REPUESTOS', 'HERRAMIENTAS')")
    }
}

/**
 * v4 → v5: categorías personalizables (con color e ícono). Crea la tabla `categoria`,
 * siembra las de siempre y migra los valores de `egreso.categoria` (nombres de enum en
 * MAYÚSCULAS) a los nombres visibles de las categorías sembradas.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `categoria` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `tipo` TEXT NOT NULL,
                `nombre` TEXT NOT NULL,
                `color` TEXT NOT NULL,
                `icono` TEXT NOT NULL,
                `orden` INTEGER NOT NULL,
                `activo` INTEGER NOT NULL
            )
            """.trimIndent(),
        )

        fun seed(tipo: String, nombre: String, color: String, icono: String, orden: Int) {
            db.execSQL(
                "INSERT INTO `categoria` (`tipo`,`nombre`,`color`,`icono`,`orden`,`activo`) VALUES (?,?,?,?,?,1)",
                arrayOf(tipo, nombre, color, icono, orden),
            )
        }
        seed("egreso", "Alimentos", "#4A7C59", "🍽️", 1)
        seed("egreso", "Transporte", "#0A6E8C", "🚗", 2)
        seed("egreso", "Servicios", "#023A5D", "🧾", 3)
        seed("egreso", "Hogar", "#98643A", "🏠", 4)
        seed("egreso", "Salud", "#C0544B", "⚕️", 5)
        seed("egreso", "Ocio", "#7A5AA6", "🎉", 6)
        seed("egreso", "Otros", "#8A8D91", "📦", 7)
        seed("ingreso", "Sueldo", "#3E8E6E", "💼", 1)
        seed("ingreso", "Ventas", "#0A6E8C", "🛒", 2)
        seed("ingreso", "Extras", "#C99A6D", "✨", 3)
        seed("ingreso", "Otros", "#8A8D91", "📦", 4)

        // Migra los valores existentes (enum en MAYÚSCULAS) al nombre visible.
        val mapa = mapOf(
            "ALIMENTOS" to "Alimentos", "TRANSPORTE" to "Transporte", "SERVICIOS" to "Servicios",
            "HOGAR" to "Hogar", "SALUD" to "Salud", "OCIO" to "Ocio", "OTROS" to "Otros",
        )
        for ((viejo, nuevo) in mapa) {
            db.execSQL("UPDATE `egreso` SET `categoria` = ? WHERE `categoria` = ?", arrayOf(nuevo, viejo))
        }
        // Cualquier categoría desconocida queda en "Otros".
        db.execSQL(
            "UPDATE `egreso` SET `categoria` = 'Otros' WHERE `categoria` NOT IN " +
                "('Alimentos','Transporte','Servicios','Hogar','Salud','Ocio','Otros')",
        )
    }
}

/**
 * v5 → v6: cuentas / medios de pago. Agrega `cuenta` a ingreso y egreso, crea la tabla
 * `cuenta`, siembra Efectivo/Banco/MercadoPago y migra el "método" de los ingresos a una cuenta.
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `ingreso` ADD COLUMN `cuenta` TEXT NOT NULL DEFAULT 'Efectivo'")
        db.execSQL("ALTER TABLE `egreso` ADD COLUMN `cuenta` TEXT NOT NULL DEFAULT 'Efectivo'")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cuenta` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `nombre` TEXT NOT NULL,
                `icono` TEXT NOT NULL,
                `saldoInicialCentavos` INTEGER NOT NULL,
                `orden` INTEGER NOT NULL,
                `activo` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        fun seed(nombre: String, icono: String, orden: Int) {
            db.execSQL(
                "INSERT INTO `cuenta` (`nombre`,`icono`,`saldoInicialCentavos`,`orden`,`activo`) VALUES (?,?,0,?,1)",
                arrayOf(nombre, icono, orden),
            )
        }
        seed("Efectivo", "💵", 1)
        seed("Banco", "🏦", 2)
        seed("MercadoPago", "💳", 3)

        // Migra el método de los ingresos existentes a una cuenta.
        db.execSQL("UPDATE `ingreso` SET `cuenta` = 'MercadoPago' WHERE `metodo` = 'MERCADO_PAGO'")
        db.execSQL("UPDATE `ingreso` SET `cuenta` = 'Efectivo' WHERE `metodo` = 'EFECTIVO'")
        db.execSQL(
            "UPDATE `ingreso` SET `cuenta` = 'Banco' WHERE `metodo` NOT IN ('EFECTIVO','MERCADO_PAGO')",
        )
    }
}

/** v7 → v8: fecha para cobrar (recordatorio) en deudas. */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `deuda` ADD COLUMN `fechaLimite` INTEGER")
    }
}

/** v10 → v11: multi-negocio. Crea la tabla negocio, siembra "Personal" y agrega negocioId a los movimientos. */
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `negocio` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `nombre` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        // Negocio por defecto con id 1 (al que se asignan los movimientos existentes).
        db.execSQL(
            "INSERT INTO `negocio` (`id`,`nombre`,`createdAt`) SELECT 1,'Personal',? WHERE NOT EXISTS (SELECT 1 FROM `negocio`)",
            arrayOf(System.currentTimeMillis()),
        )
        for (tabla in listOf("ingreso", "egreso", "deuda", "recurrente")) {
            db.execSQL("ALTER TABLE `$tabla` ADD COLUMN `negocioId` INTEGER NOT NULL DEFAULT 1")
        }
    }
}

/** v9 → v10: contactos frecuentes (clientes/proveedores) para autocompletar deudas. */
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `contacto` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `nombre` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_contacto_nombre` ON `contacto` (`nombre`)")
    }
}

/** v8 → v9: movimientos recurrentes. */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `recurrente` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `tipo` TEXT NOT NULL,
                `montoCentavos` INTEGER NOT NULL,
                `concepto` TEXT NOT NULL,
                `categoria` TEXT NOT NULL,
                `cuenta` TEXT NOT NULL,
                `diaMes` INTEGER NOT NULL,
                `activo` INTEGER NOT NULL,
                `ultimoGenerado` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }
}

/** v6 → v7: presupuesto mensual por categoría + tabla de metas de ahorro. */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `categoria` ADD COLUMN `presupuestoCentavos` INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `meta` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `nombre` TEXT NOT NULL,
                `objetivoCentavos` INTEGER NOT NULL,
                `actualCentavos` INTEGER NOT NULL,
                `fechaObjetivo` INTEGER,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }
}

/** v11 → v12: agenda (tareas, turnos y productos por fecha), por negocio. */
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `agenda` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `tipo` TEXT NOT NULL,
                `titulo` TEXT NOT NULL,
                `descripcion` TEXT NOT NULL,
                `fecha` INTEGER NOT NULL,
                `hora` TEXT NOT NULL,
                `hecho` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `negocioId` INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `ix_agenda_fecha` ON `agenda` (`fecha`)")
    }
}
