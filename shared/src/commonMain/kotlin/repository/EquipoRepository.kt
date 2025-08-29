package repository

import network.EquipoDTO

object EquipoRepository {
    // Caché en memoria para los equipos
    private var equiposCache: List<EquipoDTO>? = null
    // Flag para indicar que hubo un cambio (dirty)
    private var isDirty: Boolean = true

    // Guarda los equipos en el caché y marca la caché como actualizada
    fun saveEquipos(equipos: List<EquipoDTO>) {
        equiposCache = equipos
        isDirty = false
    }

    // Devuelve la lista de equipos guardados en memoria, o null si no existe o está sucio
    fun getEquipos(): List<EquipoDTO>? {
        return if (isDirty) null else equiposCache
    }

    // Marca el caché como sucio, de modo que la próxima consulta se realice a la API
    fun clear() {
        equiposCache = null
        isDirty = true
    }
}