package com.tallerapp.data.repository

import com.tallerapp.data.local.CategoriaDao
import com.tallerapp.data.mapper.toDomain
import com.tallerapp.data.mapper.toEntity
import com.tallerapp.domain.model.Categoria
import com.tallerapp.domain.repository.CategoriaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoriaRepositoryImpl(private val dao: CategoriaDao) : CategoriaRepository {

    override fun observar(tipo: String): Flow<List<Categoria>> =
        dao.observar(tipo).map { list -> list.map { it.toDomain() } }

    override suspend fun listar(tipo: String): List<Categoria> =
        dao.listar(tipo).map { it.toDomain() }

    override suspend fun crear(categoria: Categoria): Long = dao.insertar(categoria.toEntity())

    override suspend fun actualizar(categoria: Categoria) {
        val nombreViejo = dao.nombreDe(categoria.id)
        dao.actualizar(categoria.toEntity())
        if (nombreViejo != null && nombreViejo != categoria.nombre && categoria.tipo == "egreso") {
            dao.renombrarEnEgresos(nombreViejo, categoria.nombre)
        }
    }

    override suspend fun eliminar(id: Long): Boolean {
        val nombre = dao.nombreDe(id) ?: return false
        if (nombre.equals("Otros", ignoreCase = true)) return false
        dao.renombrarEnEgresos(nombre, "Otros")
        dao.eliminar(id)
        return true
    }
}
