package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import storage.TokenStorage

@Serializable
data class JugadorDTO(val nombreJugador: String, val dorsal: Int, val posicion: String, val idEquipo: Int, val idTemporada: Int, val id: Int)

@Serializable
data class FichaJugadorDTO(
    val id: Int,
    val idJugador: Int,
    val idTemporada: Int,
    val idEquipo: Int,
    val piernaHabil: String?,
    val caracteristicasFisicas: String?,
    val caracteristicasTacticas: String?,
    val caracteristicasTecnicas: String?,
    val conductaEntrenamiento: String?,
    val conductaConCompañeros: String?,
    val observacionFinal: String?,
    val nombreJugador: String?
)

@Serializable
data class CrearJugadorRequest(
    val nombre: String,
    val dorsal: Int,
    val posicion: String,
    val idEquipo: Int
)

@Serializable
data class ActualizarJugadorDTO(
    val dorsal: Int?,
    val posicion: String?
)

@Serializable
data class FichaJugadorRequest(
    val idJugador: Int,
    val idEquipo: Int,
    val piernaHabil: String?,
    val caracteristicasFisicas: String?,
    val caracteristicasTacticas: String?,
    val caracteristicasTecnicas: String?,
    val conductaEntrenamiento: String?,
    val conductaConCompañeros: String?,
    val observacionFinal: String?
)

@Serializable
data class LastUpdatedResponse(val lastUpdated: String?)

object JugadorApi{
    private lateinit var client: HttpClient
    private lateinit var tokenStorage: TokenStorage

    fun initialize(client: HttpClient, tokenStorage: TokenStorage) {
        this.client = client
        this.tokenStorage = tokenStorage
    }

    // Obtener jugadores de un equipo
    suspend fun getJugadoresPorEquipo(idEquipo: Int): List<JugadorDTO>? {

        return try {
            val url = "${getApiBaseUrl()}/jugadores/equipos/$idEquipo"

            println("Llamando a la API: $url")

            withTimeout(5000) {

                val response: HttpResponse = client.get(url) {
                    contentType(ContentType.Application.Json)
                }

                println("Respuesta cruda de la API: ${response.bodyAsText()}")

                if (response.status == HttpStatusCode.OK) {
                    val jugadores = response.body<List<JugadorDTO>>()
                    println("Jugadores recibidos: $jugadores")
                    jugadores
                } else {
                    println("Error en la API: ${response.status}")
                    null
                }
            }
        } catch (e: TimeoutCancellationException) {
            println("Error: La petición a la API tardó demasiado.")
            null
        } catch (e: Exception) {
            println("Error obteniendo equipos: ${e.message}")
            null
        }
    }

    suspend fun getJugadoresPorId(idJugador: Int): JugadorDTO? {

        return try {
            val url = "${getApiBaseUrl()}/jugadores/$idJugador"

            println("Llamando a la API: $url")


                val response: HttpResponse = client.get(url) {
                    contentType(ContentType.Application.Json)
                }

                println("Respuesta cruda de la API: ${response.bodyAsText()}")

                if (response.status == HttpStatusCode.OK) {
                    val jugador = response.body<JugadorDTO>()
                    println("Jugadores recibidos: $jugador")
                    jugador
                } else {
                    println("Error en la API: ${response.status}")
                    null
                }
        } catch (e: Exception) {
            println("Error obteniendo equipos: ${e.message}")
            null
        }
    }

    suspend fun crearJugador(nombreJugador: String, dorsal: Int, posicion: String, idEquipo: Int): JugadorDTO? {
        val url = "${getApiBaseUrl()}/jugadores"

        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(CrearJugadorRequest(nombreJugador, dorsal, posicion, idEquipo))
            }

            if (response.status == HttpStatusCode.OK) {
                val jugadorDTO = response.body<JugadorDTO>()
                jugadorDTO
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error creando jugador: ${e.message}")
            null
        }
    }

    suspend fun eliminarJugador(idJugador: Int): Boolean {
        val url = "${getApiBaseUrl()}/jugadores/$idJugador"

        return try {
            val response: HttpResponse = client.delete(url)

            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            println("Error eliminando jugador: ${e.message}")
            false
        }
    }

    suspend fun actualizarJugador(id: Int, dorsal: Int, posicion: String): Boolean {
        val url = "${getApiBaseUrl()}/jugadores/$id"

        return try {
            val response: HttpResponse = client.put(url) {
                contentType(ContentType.Application.Json)
                setBody(ActualizarJugadorDTO(dorsal, posicion))
            }

            if (response.status == HttpStatusCode.OK) {
                true
            } else {
                false
            }
        } catch (e: Exception) {
            println("Error actualizando el jugador: ${e.message}")
            false
        }
    }

    suspend fun crearFichaJugador(idJugador: Int, idEquipo: Int, piernaHabil: String?, caracteristicasFisicas: String?, caracteristicasTacticas: String?, caracteristicasTecnicas: String?, conductaEntrenamiento: String?, conductaConCompañeros: String?, observacionFinal: String?): FichaJugadorDTO? {
        val url = "${getApiBaseUrl()}/ficha-jugador"

        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(FichaJugadorRequest(idJugador, idEquipo, piernaHabil, caracteristicasFisicas, caracteristicasTacticas, caracteristicasTecnicas, conductaEntrenamiento, conductaConCompañeros, observacionFinal))
            }

            if (response.status == HttpStatusCode.OK) {
                val fichaJugadorDTO = response.body<FichaJugadorDTO>()
                fichaJugadorDTO
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error creando ficha de jugador: ${e.message}")
            null
        }
    }

    suspend fun eliminarFichaJugador(idFichaJugador: Int): Boolean {
        val url = "${getApiBaseUrl()}/ficha-jugador/$idFichaJugador"

        return try {
            val response: HttpResponse = client.delete(url)

            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            println("Error eliminando ficha del jugador: ${e.message}")
            false
        }
    }

    suspend fun obtenerFichaDeJugador(idJugador: Int): FichaJugadorDTO? {
        val url = "${getApiBaseUrl()}/ficha-jugador/jugador/$idJugador"

        return try {
            println("Llamando a la API: $url")

            withTimeout(5000) {

                val response: HttpResponse = client.get(url) {
                    contentType(ContentType.Application.Json)
                }

                println("Respuesta cruda de la API: ${response.bodyAsText()}")

                if (response.status == HttpStatusCode.OK) {
                    val fichaJugador = response.body<FichaJugadorDTO>()
                    println("Jugadores recibidos: $fichaJugador")
                    fichaJugador
                } else {
                    println("Error en la API: ${response.status}")
                    null
                }
            }
        } catch (e: TimeoutCancellationException) {
            println("Error: La petición a la API tardó demasiado.")
            null
        } catch (e: Exception) {
            println("Error obteniendo la ficha del jugador: ${e.message}")
            null
        }
    }

    suspend fun obtenerFichaDeJugadoresPorEquipo(idEquipo: Int): List<FichaJugadorDTO> {
        val url = "${getApiBaseUrl()}/ficha-jugador/equipo/$idEquipo"

        return try {
            println("Llamando a la API: $url")

            withTimeout(5000) {

                val response: HttpResponse = client.get(url) {
                    contentType(ContentType.Application.Json)
                }

                println("Respuesta cruda de la API: ${response.bodyAsText()}")

                if (response.status == HttpStatusCode.OK) {
                    val fichaJugadores = response.body<List<FichaJugadorDTO>>()
                    println("Jugadores recibidos: $fichaJugadores")
                    fichaJugadores
                } else {
                    println("Error en la API: ${response.status}")
                    emptyList()
                }
            }
        } catch (e: TimeoutCancellationException) {
            println("Error: La petición a la API tardó demasiado.")
            emptyList()
        } catch (e: Exception) {
            println("Error obteniendo las fichas de los jugadores: ${e.message}")
            emptyList()
        }
    }

    suspend fun getLastUpdated(idEquipo: Int): String? {
        val response = client.get("${getApiBaseUrl()}/equipos/$idEquipo/last-updated")
        return if (response.status == HttpStatusCode.OK) {
            // Obtener la fecha de la respuesta
            val responseBody = response.body<Map<String, String>>()
            responseBody["lastUpdated"]
        } else {
            null
        }
    }
}