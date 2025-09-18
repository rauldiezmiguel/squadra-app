package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import storage.TokenStorage

@Serializable
data class TemporadaDTO(
    val id: Int,
    val añoInicio: Int,
    val añoFin: Int,
    val activa: Boolean
)

object TemporadasApi {
    private lateinit var client: HttpClient

    fun initialize(client: HttpClient) {
        this.client = client
    }

    suspend fun getAllTemporadas(): List<TemporadaDTO>{
        return try {
            val url = "${getApiBaseUrl()}/temporadas"

            val response: HttpResponse = TemporadasApi.client.get(url){
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK){
                val temporadas = response.body<List<TemporadaDTO>>()
                temporadas
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error obteniendo las temporadas. ${e.message}")
            emptyList()
        }
    }
}