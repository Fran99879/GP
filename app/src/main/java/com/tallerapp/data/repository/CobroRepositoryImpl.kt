package com.tallerapp.data.repository

import androidx.room.withTransaction
import com.tallerapp.data.local.IngresoDao
import com.tallerapp.data.local.TallerDatabase
import com.tallerapp.data.local.TrabajoDao
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.EstadoCobro
import com.tallerapp.domain.model.Ingreso
import com.tallerapp.domain.repository.CobroRepository

/**
 * Cobro/anulación como transacciones atómicas sobre las tablas trabajo + ingreso.
 * Si algo falla, nada se persiste (evita ingresos huérfanos o trabajos "cobrados"
 * sin ingreso) — mitigación de RT-1.
 */
class CobroRepositoryImpl(
    private val database: TallerDatabase,
    private val trabajoDao: TrabajoDao,
    private val ingresoDao: IngresoDao,
) : CobroRepository {

    override suspend fun registrarCobro(ingreso: Ingreso, trabajoId: Long): Long =
        database.withTransaction {
            val ingresoId = ingresoDao.insertar(ingreso.toEntity())
            val trabajo = trabajoDao.obtener(trabajoId)
            if (trabajo != null) {
                trabajoDao.actualizar(
                    trabajo.copy(estadoCobro = EstadoCobro.COBRADO.name, cobroId = ingresoId),
                )
            }
            ingresoId
        }

    override suspend fun anularCobro(trabajoId: Long): Boolean =
        database.withTransaction {
            val trabajo = trabajoDao.obtener(trabajoId) ?: return@withTransaction false
            val ingresoId = trabajo.cobroId ?: return@withTransaction false
            ingresoDao.eliminar(ingresoId)
            trabajoDao.actualizar(
                trabajo.copy(estadoCobro = EstadoCobro.PENDIENTE_DE_COBRO.name, cobroId = null),
            )
            true
        }
}
