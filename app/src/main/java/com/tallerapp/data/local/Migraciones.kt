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
