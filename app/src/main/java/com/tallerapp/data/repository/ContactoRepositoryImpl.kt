package com.tallerapp.data.repository

import com.tallerapp.data.local.ContactoDao
import com.tallerapp.data.local.ContactoEntity
import com.tallerapp.domain.repository.ContactoRepository
import kotlinx.coroutines.flow.Flow

class ContactoRepositoryImpl(private val dao: ContactoDao) : ContactoRepository {
    override fun observar(): Flow<List<String>> = dao.observar()
    override suspend fun agregar(nombre: String) {
        val limpio = nombre.trim()
        if (limpio.isNotEmpty()) dao.insertar(ContactoEntity(nombre = limpio, createdAt = System.currentTimeMillis()))
    }
}
