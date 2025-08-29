package repository

import network.JugadorDTO

object JugadorRepository {
    private val jugadoresCachePorEquipo = mutableMapOf<Int, List<JugadorDTO>>()
    private var isDirty: Boolean = true

    // Guarda los jugadores en caché
    fun saveJugadores(idEquipo: Int, jugadores: List<JugadorDTO>) {
        jugadoresCachePorEquipo[idEquipo] = jugadores
        isDirty = false
    }

    // Devuelve los jugadores si el caché es válido
    fun getJugadores(idEquipo: Int): List<JugadorDTO>? {
        return if (isDirty) null else jugadoresCachePorEquipo[idEquipo]
    }

    // Limpia la caché
    fun clearEquipo(idEquipo: Int) {
        jugadoresCachePorEquipo.remove(idEquipo)
        isDirty = true
    }
}
