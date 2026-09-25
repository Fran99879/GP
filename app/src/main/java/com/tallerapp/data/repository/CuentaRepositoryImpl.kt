package com.tallerapp.data.repository

import com.tallerapp.data.local.CuentaDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.Cuenta
import com.tallerapp.domain.repository.CuentaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CuentaRepositoryImpl(private val dao: CuentaDao) : CuentaRepository {

    override fun observar(): Flow<List<Cuenta>> =
        dao.observar().map { list -> list.map { it.toDomain() } }

    override suspend fun listar(): List<Cuenta> = dao.listar().map { it.toDomain() }

    override suspend fun saldos(): List<Cuenta> {
        val ing = dao.sumaIngresosPorCuenta().associate { it.cuenta to it.total }
        val gas = dao.sumaEgresosPorCuenta().associate { it.cuenta to it.total }
        return dao.listar().map { e ->
            val c = e.toDomain()
            c.copy(saldoCentavos = c.saldoInicialCentavos + (ing[c.nombre] ?: 0) - (gas[c.nombre] ?: 0))
        }
    }

    override suspend fun crear(cuenta: Cuenta): Long = dao.insertar(cuenta.toEntity())

    override suspend fun actualizar(cuenta: Cuenta) {
        val viejo = dao.nombreDe(cuenta.id)
        dao.actualizar(cuenta.toEntity())
        if (viejo != null && viejo != cuenta.nombre) {
            dao.renombrarEnIngresos(viejo, cuenta.nombre)
            dao.renombrarEnEgresos(viejo, cuenta.nombre)
        }
    }

    override suspend fun eliminar(id: Long): Boolean {
        val nombre = dao.nombreDe(id) ?: return false
        if (nombre.equals("Efectivo", ignoreCase = true)) return false
        dao.renombrarEnIngresos(nombre, "Efectivo")
        dao.renombrarEnEgresos(nombre, "Efectivo")
        dao.eliminar(id)
        return true
    }
}
