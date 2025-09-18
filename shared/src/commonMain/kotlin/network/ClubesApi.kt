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
import kotlinx.serialization.Serializable
import storage.TokenStorage

@Serializable
data class ClubDTO(
    val id: Int,
    val nombreClub: String,
    val direccion: String?,
    val telefono: String?,
    val localizacion: String?
)

@Serializable
data class CreateClubRequest(
    val nombreClub: String,
    val direccion: String?,
    val telefono: String?,
    val localizacion: String?
)

object ClubesApi {
    private lateinit var client: HttpClient

    fun initialize(client: HttpClient) {
        this.client = client
    }

    suspend fun getAllClubes(): List<ClubDTO>{
        return try {
            val url = "${getApiBaseUrl()}/clubes"

            val response: HttpResponse = client.get(url){
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK){
                val clubes = response.body<List<ClubDTO>>()
                clubes
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error obteniendo los clubes. ${e.message}")
            emptyList()
        }
    }

    suspend fun createClub(nombreClub: String, direccion: String?, telefono: String?, localizacion: String?): Boolean {
        return try {
            val url = "${getApiBaseUrl()}/clubes"

            val response: HttpResponse = client.post(url){
                contentType(ContentType.Application.Json)
                setBody(CreateClubRequest(nombreClub, direccion, telefono, localizacion))
            }

            response.status == HttpStatusCode.Created
        } catch (e: Exception) {
            println("Error creando un club nuevo ${e.message}")
            false
        }
    }

    suspend fun actualizarClub(id: Int, nombreClub: String, direccion: String?, telefono: String?, localizacion: String?): Boolean {
        return try {
            val url = "${getApiBaseUrl()}/clubes/$id"

            val response: HttpResponse = client.put(url){
                contentType(ContentType.Application.Json)
                setBody(CreateClubRequest(nombreClub, direccion, telefono, localizacion))
            }

            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            println("Error al actualizar el club con ID $id. ${e.message}")
            false
        }
    }

    suspend fun eliminarClub(id: Int): Boolean {
        return try {
            val url = "${getApiBaseUrl()}/clubes/$id"

            val response: HttpResponse = client.delete(url){
                contentType(ContentType.Application.Json)
            }

            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            println("Error al eliminar el club con ID $id. ${e.message}")
            false
        }
    }
}