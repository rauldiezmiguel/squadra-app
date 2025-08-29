package network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import storage.TokenStorage

@Serializable
data class EquipoDTO(val id: Int, val nombreEquipo: String, val categoria: String, val subcategoria: String, val idClub: Int, val idTemporada: Int)

object EquipoApi {
    private lateinit var client: HttpClient
    private lateinit var tokenStorage: TokenStorage

    fun initialize(client: HttpClient, tokenStorage: TokenStorage) {
        this.client = client
        this.tokenStorage = tokenStorage
    }

    // Obtener equipos según el rol del usuario
    suspend fun getEquiposForUser(): List<EquipoDTO>? {
        val userId = tokenStorage.getUserId() ?: return null
        val userRole = tokenStorage.getUserRole() ?: return null
        val userClubId = tokenStorage.getClubId() ?: return null

        println("User ID: $userId")
        println("User role: $userRole")
        println("User club ID: $userClubId")

        return try {
            val url = when (userRole.lowercase()) {
                "entrenador" -> "${getApiBaseUrl()}/entrenadores/entrenador/$userId"
                "coordinador" -> "${getApiBaseUrl()}/equipos/clubes/$userClubId"
                else -> return null
            }

            println("Llamando a la API: $url")

            withTimeout(10000) {

                val response: HttpResponse = client.get(url) {
                    contentType(ContentType.Application.Json)
                }

                println("Respuesta cruda de la API: ${response.bodyAsText()}")

                if (response.status == HttpStatusCode.OK) {
                    val equipos = response.body<List<EquipoDTO>>()
                    println("Equipos recibidos: $equipos")
                    equipos
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

    suspend fun getCategoriaById(id: Int): String? {
        return try {
            val url = "${getApiBaseUrl()}/equipos/$id/categoria"

            println("Llamando a la API: $url")

            val response: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }

            println("Respuesta cruda de la API: ${response.bodyAsText()}")

            if (response.status == HttpStatusCode.OK) {
                val categoria = response.body<String>()
                println("Categoria: $categoria")
                categoria
            } else {
                println("Error en la API: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("Error obteniendo la categoria: ${e.message}")
            null
        }
    }
}