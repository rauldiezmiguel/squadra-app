package network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
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
data class PerfilUsuarioDTO(
    val id: Int,
    val nombreUsuario: String,
    val tipoUsuario: String,
    val club: ClubPerfilDTO?,
    val equipos: List<EquipoPerfilDTO>
)

@Serializable
data class ClubPerfilDTO(
    val id: Int,
    val nombre: String
)

@Serializable
data class EquipoPerfilDTO(
    val id: Int,
    val nombreEquipo: String
    // añade aquí los campos que necesites
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

object UsuarioApi {
    private lateinit var client: HttpClient
    private lateinit var tokenStorage: TokenStorage

    fun initialize(client: HttpClient, tokenStorage: TokenStorage) {
        this.client = client
        this.tokenStorage = tokenStorage
    }

    suspend fun getPerfilUsuario(): PerfilUsuarioDTO? {
        val userId = tokenStorage.getUserId() ?: return null

        return try {
            val url = "${getApiBaseUrl()}/usuarios/perfil/$userId"
            println(url)

            val response: HttpResponse = client.get(url) {
                contentType(ContentType.Application.Json)
            }

            if (response.status == HttpStatusCode.OK) {
                val perfil = response.body<PerfilUsuarioDTO>()
                perfil
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error obteniendo el perfil del usuario. ${e.message}")
            null
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Boolean {
       return try {
           val url = "${getApiBaseUrl()}/usuarios/change-password"
           print(url)

           val response: HttpResponse = client.post(url) {
               contentType(ContentType.Application.Json)
               setBody(ChangePasswordRequest(currentPassword, newPassword))
           }

           response.status == HttpStatusCode.OK
       } catch (e: Exception) {
           println("Error cambiando la contraseña. ${e.message}")
           false
       }
    }
}