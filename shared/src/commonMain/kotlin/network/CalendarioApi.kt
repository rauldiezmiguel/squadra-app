package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import storage.TokenStorage

@Serializable
sealed class EventoCalendario {
    abstract val idEquipo: Int
    abstract val fecha: LocalDate

    @Serializable
    @kotlinx.serialization.SerialName("entrenamiento")
    data class Entrenamiento(
        override val idEquipo: Int,
        override val fecha: LocalDate,
        val descripcion: String?,
        val entrenamientoUrl: String?,
        val idEntrenamiento: Int

    ) : EventoCalendario()

    @Serializable
    @kotlinx.serialization.SerialName("partido")
    data class Partido(
        override val idEquipo: Int,
        override val fecha: LocalDate,
        val nombreRival: String,
        val resultadoNumerico: String?,
        val resultado: String?,
        val jugadoresDestacados: String?,
        val idPartido: Int
    ) : EventoCalendario()
}

@Serializable
data class PartidoDTO(
    val id: Int,
    val idEquipo: Int,
    val idTemporada: Int,
    val nombreRival: String,
    val fecha: LocalDate,
    val resultadoNumerico: String?,
    val resultado: String?,
    val jugadoresDestacados: String?,
    val cuartosEquipo: List<CuartosEquipoDTO> = emptyList(),
    val cuartosRival: List<CuartosRivalDTO> = emptyList()
)

@Serializable
data class EntrenamientoDTO(
    val id: Int,
    val fecha: LocalDate,
    val descripcion: String?,
    val entrenamientoUrl: String?,
    val idEquipo: Int,
    val idTemporada: Int
)

@Serializable
data class CrearEntrenamientoRequest(
    val idEquipo: Int,
    val fecha: LocalDate,
    val descripcion: String?,
    val entrenamientoUrl: String?
)

@Serializable
data class CrearPartidoRequest(
    val idEquipo: Int,
    val nombreRival: String,
    val fecha: LocalDate
)

@Serializable
data class ActualizarResultadoPartidoRequest(
    val resultadoNumerico: String,
    val resultado: String
)

@Serializable
data class ActualizarJugadoresDestacadosRequest(
    val jugadoresDestacados: String
)

enum class ResultadoPartido(val label: String) {
    VICTORIA("Victoria"),
    DERROTA("Derrota"),
    EMPATE("Empate")
}

object CalendarioApi {
    private lateinit var client: HttpClient
    private lateinit var tokenStorage: TokenStorage

    fun initialize(client: HttpClient, tokenStorage: TokenStorage) {
        this.client = client
        this.tokenStorage = tokenStorage
    }

    suspend fun getEventosPorEquipo(idEquipo: Int): List<EventoCalendario> {
        val entrenamientos = getEntrenamientos(idEquipo)
        val partidos = getPartidos(idEquipo)
        return entrenamientos + partidos
    }

    private suspend fun getEntrenamientos(idEquipo: Int): List<EventoCalendario.Entrenamiento> {
        val url = "${getApiBaseUrl()}/entrenamientos/equipos/$idEquipo"
        return try {
            val response: HttpResponse = client.get(url){
                contentType(ContentType.Application.Json)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<List<EntrenamientoDTO>>().map {
                    EventoCalendario.Entrenamiento(
                        fecha = it.fecha,
                        descripcion = it.descripcion,
                        entrenamientoUrl = it.entrenamientoUrl,
                        idEquipo = it.idEquipo,
                        idEntrenamiento = it.id
                    )
                }
            } else emptyList()
        } catch (e: Exception) {
            println("Error obteniendo entrenamientos: ${e.message}")
            emptyList()
        }
    }

    suspend fun getPartidos(idEquipo: Int): List<EventoCalendario.Partido> {
        val url = "${getApiBaseUrl()}/partidos/equipos/$idEquipo"
        return try {
            val response: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<List<PartidoDTO>>().map {
                    EventoCalendario.Partido(
                        nombreRival = it.nombreRival,
                        fecha = it.fecha,
                        resultadoNumerico = it.resultadoNumerico,
                        resultado = it.resultado,
                        jugadoresDestacados = it.jugadoresDestacados,
                        idEquipo = it.idEquipo,
                        idPartido = it.id
                    )
                }
            } else emptyList()
        } catch (e: Exception) {
            println("Error obteniendo partidos: ${e.message}")
            emptyList()
        }
    }

    suspend fun crearEntrenamiento(idEquipo: Int, fecha: LocalDate, descripcion: String?, entrenamientoUrl: String?): EntrenamientoDTO? {
        val url = "${getApiBaseUrl()}/entrenamientos"
        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(CrearEntrenamientoRequest(idEquipo, fecha, descripcion, entrenamientoUrl))
            }

            if (response.status == HttpStatusCode.OK) {
                val entrenamientoDTO = response.body<EntrenamientoDTO>()
                entrenamientoDTO
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error creando entrenamiento: ${e.message}")
            null
        }
    }

    suspend fun eliminarEntrenamiento(id: Int): Boolean {
        val url = "${getApiBaseUrl()}/entrenamientos/$id"

        return try {
            val response = client.delete(url)
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            println("Error eliminando entrenamiento con id $id: ${e.message}")
            false
        }
    }

    suspend fun crearPartido(idEquipo: Int, nombreRival: String, fecha: LocalDate): PartidoDTO? {
        val url = "${getApiBaseUrl()}/partidos"

        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(CrearPartidoRequest(idEquipo, nombreRival, fecha))
            }

            if (response.status == HttpStatusCode.OK){
                val partidoDTO = response.body<PartidoDTO>()
                partidoDTO
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error creando el partido: ${e.message}")
            null
        }
    }

    suspend fun actualizarPartido(id: Int, resultadoNumerico: String, resultado: String): PartidoDTO? {
        val url = "${getApiBaseUrl()}/partidos/resultado/$id"
        println(url)

        return try {
            val response: HttpResponse = client.put(url) {
                contentType(ContentType.Application.Json)
                setBody(ActualizarResultadoPartidoRequest(resultadoNumerico, resultado))
            }

            if (response.status == HttpStatusCode.OK){
                val partidoDTO = response.body<PartidoDTO>()
                partidoDTO
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error actualizando partido: ${e.message}")
            null
        }
    }

    suspend fun actualizarJugadoresDestacados(id: Int, jugadoresDestacados: String): PartidoDTO? {
        val url = "${getApiBaseUrl()}/partidos/jugadores-destacados/$id"
        println(url)

        return try {
            val response: HttpResponse = client.put(url) {
                contentType(ContentType.Application.Json)
                setBody(ActualizarJugadoresDestacadosRequest(jugadoresDestacados))
            }

            if (response.status == HttpStatusCode.OK){
                val partidoDTO = response.body<PartidoDTO>()
                partidoDTO
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error actualizando partido: ${e.message}")
            null
        }
    }

    suspend fun eliminarPartido(id: Int): Boolean {
        val url = "${getApiBaseUrl()}/partidos/$id"

        return try {
            val response = client.delete(url)
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            println("Error eliminando partido: ${e.message}")
            false
        }
    }

    suspend fun getPartidosByEquipo(idEquipo: Int): List<PartidoDTO> {
        val url = "${getApiBaseUrl()}/partidos/$idEquipo"
        return try {
            val response = client.get(url)
           if (response.status == HttpStatusCode.OK) {
               response.body()
           } else {
               emptyList()
           }
        } catch (e: Exception) {
            println("Error obteniendo partidos: ${e.message}")
            emptyList()
        }
    }
}