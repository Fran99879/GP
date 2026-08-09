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
