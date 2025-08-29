package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
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
data class EvaluacionDTO(
    val id: Int,
    val idJugador: Int,
    val idTemporada: Int? = null,
    val fecha: String? = null, // formato "YYYY-MM-DD"
    val comportamiento: Int,
    val tecnica: Int,
    val tactica: Int,
    val observaciones: String?
)

@Serializable
data class CrearEvaluacionRequest(
    val idJugador: Int,
    val fecha: String,
    val comportamiento: Int,
    val tecnica: Int,
    val tactica: Int,
    val observaciones: String
)

@Serializable
data class PromedioMensualDTO(
    val año: Int,
    val mes: Int,
    val comportamiento: Double,
    val tecnica: Double,
    val tactica: Double
)

object EvaluacionApi {
    private lateinit var client: HttpClient
    private lateinit var tokenStorage: TokenStorage

    fun initialize(client: HttpClient, tokenStorage: TokenStorage) {
        this.client = client
        this.tokenStorage = tokenStorage
    }

    suspend fun getEvaluaciones(idJugador: Int): List<EvaluacionDTO>? {
        val url = "${getApiBaseUrl()}/evaluaciones/$idJugador"

        return try {
            val response: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                response.body()
            } else null
        } catch (e: Exception) {
            println("Error obteniendo evaluaciones: ${e.message}")
            null
        }
    }

    suspend fun getPromediosMensuales(idJugador: Int): List<PromedioMensualDTO>? {
        val url = "${getApiBaseUrl()}/evaluaciones/promedios/$idJugador"

        return try {
            val response: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                response.body()
            } else null
        } catch (e: Exception) {
            println("Error obteniendo promedios mensuales: ${e.message}")
            null
        }
    }

    suspend fun crearEvaluacion(request: CrearEvaluacionRequest): EvaluacionDTO? {
        val url = "${getApiBaseUrl()}/evaluaciones"

        return try {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.Created) {
                response.body()
            } else null
        } catch (e: Exception) {
            println("Error creando evaluación: ${e.message}")
            null
        }
    }

    suspend fun eliminarEvaluacion(idEvaluacion: Int): Boolean {
        val url = "${getApiBaseUrl()}/evaluaciones/$idEvaluacion"

        return try {
            val response = client.delete(url)
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            println("Error eliminando evaluación: ${e.message}")
            false
        }
    }
}