package com.tallerapp.domain.usecase

import com.tallerapp.core.billing.EstadoPlan
import com.tallerapp.domain.model.Contacto
import com.tallerapp.domain.model.TipoContacto
import com.tallerapp.domain.repository.ContactoRepository
import kotlinx.coroutines.flow.Flow

/** Clientes o proveedores del negocio activo, filtrados por el texto de búsqueda. */
class ObservarContactosPorTipoUseCase(private val repository: ContactoRepository) {
    operator fun invoke(tipo: String, busqueda: String = ""): Flow<List<Contacto>> =
        repository.observar(tipo, busqueda)
}

/** Nombres nada más: es lo que necesita el autocompletado del formulario de deudas. */
class ObservarContactosUseCase(private val repository: ContactoRepository) {
    operator fun invoke(): Flow<List<String>> = repository.observarNombres()
}

class ObtenerContactoUseCase(private val repository: ContactoRepository) {
    suspend operator fun invoke(id: Long): Contacto? = repository.obtener(id)
}

/**
 * Alta rápida por nombre: la usan el formulario de deudas y la factura, donde el usuario
 * escribe un nombre suelto y no debería tener que ir a cargar una ficha.
 *
 * Devuelve el id del contacto, sea el que ya existía o el recién creado. No pide Pro:
 * anotar a quién le fiaste es parte del uso personal, que es gratis. El gate está en la
 * ficha completa ([GuardarContactoUseCase]).
 */
class AgregarContactoUseCase(private val repository: ContactoRepository) {
    suspend operator fun invoke(nombre: String, tipo: String = TipoContacto.CLIENTE): Long? {
        val limpio = nombre.trim()
        if (limpio.isEmpty()) return null

        val existente = repository.porNombre(limpio)
        if (existente != null) {
            // Si ya estaba como cliente y ahora aparece como proveedor (o al revés), sirve
            // para las dos cosas: es el mismo negocio del otro lado del mostrador.
            if (existente.tipo != tipo && existente.tipo != TipoContacto.AMBOS) {
                repository.actualizar(existente.copy(tipo = TipoContacto.AMBOS))
            }
            return existente.id
        }
        return repository.crear(Contacto(nombre = limpio, tipo = tipo))
    }
}

/** Resultado de guardar la ficha de un contacto. */
sealed interface ResultadoGuardarContacto {
    data class Guardado(val id: Long) : ResultadoGuardarContacto

    /** Las fichas de clientes y proveedores son parte de Pro (ver `PLANES.md`). */
    data object RequierePro : ResultadoGuardarContacto

    data object NombreVacio : ResultadoGuardarContacto

    /** Ya hay otro contacto con ese nombre en el negocio. */
    data class NombreRepetido(val existente: Contacto) : ResultadoGuardarContacto
}

/**
 * Crea o actualiza la ficha completa.
 *
 * El nombre repetido se avisa en vez de dejarlo fallar contra el índice único: el error de
 * SQLite no le dice nada al usuario, y acá se puede ofrecer abrir el que ya existe.
 */
class GuardarContactoUseCase(private val repository: ContactoRepository) {
    suspend operator fun invoke(contacto: Contacto): ResultadoGuardarContacto {
        if (!EstadoPlan.esPro) return ResultadoGuardarContacto.RequierePro
        if (contacto.nombre.isBlank()) return ResultadoGuardarContacto.NombreVacio

        val limpio = contacto.copy(
            nombre = contacto.nombre.trim(),
            documento = contacto.documento.trim(),
            telefono = contacto.telefono.trim(),
            email = contacto.email.trim(),
            direccion = contacto.direccion.trim(),
            nota = contacto.nota.trim(),
        )

        val existente = repository.porNombre(limpio.nombre)
        if (existente != null && existente.id != limpio.id) {
            return ResultadoGuardarContacto.NombreRepetido(existente)
        }

        return if (limpio.id == 0L) {
            ResultadoGuardarContacto.Guardado(repository.crear(limpio))
        } else {
            repository.actualizar(limpio)
            ResultadoGuardarContacto.Guardado(limpio.id)
        }
    }
}

class EliminarContactoUseCase(private val repository: ContactoRepository) {
    suspend operator fun invoke(id: Long) = repository.eliminar(id)
}
