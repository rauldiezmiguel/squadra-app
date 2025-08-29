package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import storage.TokenStorage

@Serializable
data class CrearEstadisticasRequest(
    val idJugador: Int,
    val idPartido: Int,
    val minutosJugados: Int,
    val goles: Int,
    val asistencias: Int,
    val titular: Boolean?,
    val tarjetasAmarillas: Int,
    val tarjetasRojas: Int,
    val partidoJugado: Boolean
)

data class EstadisticasTotalesJugador(
    val goles: Int,
    val asistencias: Int,
    val minutosJugados: Int,
    val partidosJugados: Int,
    val titularidades: Int,
    val tarjetasAmarillas: Int,
    val tarjetasRojas: Int
)

@Serializable
data class EstadisticasJugadorDTO(
    val id: Int,
    val idJugador: Int,
    val idPartido: Int,
    val idTemporada: Int,
    val minutosJugados: Int,
    val goles: Int,
    val asistencias: Int,
    val titular: Boolean,
    val tarjetasAmarillas: Int,
    val tarjetasRojas: Int,
    val partidoJugado: Boolean
)

@Serializable
data class EstadisticasTotalesJugadorDTO(
    val idJugador: Int,
    val idTemporada: Int,
    val goles: Int,
    val asistencias: Int,
    val minutosJugados: Int,
    val partidosJugados: Int,
    val partidosComoTitular: Int,
    val tarjetasAmarillas: Int,
    val tarjetasRojas: Int
)

@Serializable
data class EstadisticaPartidoDTO(
    val idPartido: Int,
    val valorEstadistica: Int,
    val nomEstadistica: String,
    val nombreRival: String
)

@Serializable
data class EstadisticasTotalesEquipoDTO(
    val idEquipo: Int,
    val idTemporada: Int,
    val golesTotales: Int,
    val asistenciasTotales: Int,
    val minutosTotales: Int,
    val partidosTotales: Int,
    val tarjetasAmarillasTotales: Int,
    val tarjetasRojasTotales: Int
)

@Serializable
data class EstadisticaEquipoDetalleDTO(
    val idJugador: Int,
    val idEquipo: Int,
    val nombreJugador: String,
    val valorEstadistica: Int,
    val nomEstadistica: String
)

@Serializable
data class EstadisticaEquipoDTO(
    val nomEstadistica: String,
    val totalEquipo: Int,
    val detallesPorJugador: List<EstadisticaEquipoDetalleDTO>
)

object EstadisticasApi {
    private lateinit var client: HttpClient
    private lateinit var tokenStorage: TokenStorage

    fun initialize(client: HttpClient, tokenStorage: TokenStorage) {
        this.client = client
        this.tokenStorage = tokenStorage
    }

    /**
     * Envia una petición POST para crear una estadística.
     * Devuelve true si la operación fue exitosa (HTTP 200), false en otro caso.
     */
    suspend fun crearEstadistica(request: CrearEstadisticasRequest): Boolean {
        val url = "${getApiBaseUrl()}/estadisticas-jugador"
        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            println("Error al crear estadística: ${e.message}")
            false
        }
    }

    suspend fun getEstadisticasTotalesJugador(idJugador: Int, idTemporada: Int): List<EstadisticasTotalesJugadorDTO> {
        val url = "${getApiBaseUrl()}/estadisticas-total-jugador/$idJugador/temporada/$idTemporada"

        return try {
            val response: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                // Deserializa directamente al tipo deseado
                response.body()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error al intentar obtener estadisticas totales jugador: ${e.message}")
            emptyList()
        }
    }

    suspend fun getEstadisticasByJugadorByPartido(idJugador: Int, idPartido: Int): List<EstadisticasJugadorDTO> {
        val url = "${getApiBaseUrl()}/estadisticas-jugador/$idJugador/partidos/$idPartido"
        println(url)

        return try {
            val response: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                response.body()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error al intentar obtener estadísticas jugador para un partido: ${e.message}")
            emptyList()
        }
    }

    suspend fun getEstadisticaPartido(
        idPartido: Int,
        nombEstadistica: String
    ): EstadisticaPartidoDTO? {
        val url = "${getApiBaseUrl()}/estadisticas-jugador/$idPartido/detalle/$nombEstadistica"
        return try {
            val resp = client.get(url) {
                contentType(ContentType.Application.Json)
            }
            if (resp.status == HttpStatusCode.OK) resp.body()
            else null
        } catch (e: Exception) {
            println("Error fetching detalle estadistica: ${e.message}")
            null
        }
    }

    /**
     * Obtiene la lista de estadísticas por partido para un equipo y temporada dados,
     * filtrando por el nombre de la estadística (goles, asistencias, etc.).
     */
    suspend fun getDetalleEstadisticaEquipo(
        idEquipo: Int,
        idTemporada: Int,
        nomEstadistica: String
    ): List<EstadisticaPartidoDTO> {
        val encodedEstadistica = nomEstadistica.replace(" ", "")
        val url = "${getApiBaseUrl()}/estadisticas-partidos/$idEquipo/temporada/$idTemporada/detalle/$encodedEstadistica"
        println(url)
        return try {
            val resp: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }
            if (resp.status == HttpStatusCode.OK) {
                resp.body()  // Ktor deserializa a List<EstadisticaPartidoDTO>
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error al obtener detalle estadística: ${e.message}")
            emptyList()
        }
    }

    /**
     * Llama a:
     * GET /estadisticas-jugador/{idJugador}/equipo/{idEquipo}/temporada/{idTemporada}/detalle/{nomEstadistica}
     */
    suspend fun getDetalleEstadisticaJugador(
        idJugador: Int,
        idEquipo: Int,
        idTemporada: Int,
        nomEstadistica: String
    ): List<EstadisticaPartidoDTO> {
        val encodedEstadistica = nomEstadistica.replace(" ", "")
        val url = "/estadisticas-jugador/$idJugador/equipo/$idEquipo/temporada/$idTemporada/detalle/$encodedEstadistica"
        println("${getApiBaseUrl()}$url")
        return client.get("${getApiBaseUrl()}$url").body()
    }

    suspend fun getEstadisticaEquipoDetalle(
        idEquipo: Int,
        idTemporada: Int,
        nomEstadistica: String
    ): List<EstadisticaEquipoDetalleDTO> {
        val encodedEstadistica = nomEstadistica.replace(" ", "")
        val url = "${getApiBaseUrl()}/estadisticas-equipo/$idEquipo/temporada/$idTemporada/detalle/$encodedEstadistica"
        return client.get(url).body()
    }

    suspend fun getDetalleEstadisticaEquipoAgregado(
        idEquipo: Int,
        idTemporada: Int,
        nomEstadistica: String
    ): EstadisticaEquipoDTO? {
        val encodedEstadistica = nomEstadistica.replace(" ", "")
        val url = "${getApiBaseUrl()}/estadisticas-equipo/$idEquipo/temporada/$idTemporada/detalle-agregado/${encodedEstadistica}"

        println(url)
        return try {
            client.get(url) {
                contentType(ContentType.Application.Json)
            }.body()
        } catch (e: Exception) {
            println("Error al obtener estadística de equipo agregado: ${e.message}")
            null
        }
    }

    suspend fun getEstadisticasEquipoByTemporada(idEquipo: Int, idTemporada: Int): List<EstadisticasTotalesEquipoDTO> {

        val url = "${getApiBaseUrl()}/estadisticas-equipo/$idEquipo/temporada/$idTemporada"
        println(url)

        return try {
            client.get(url) {
                contentType(ContentType.Application.Json)
            }.body()
        } catch (e: Exception) {
            println("Error al obtener estadísticas del equipo: ${e.message}")
            emptyList()
        }
    }
}
