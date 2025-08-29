package network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.setBody
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class AlineacionRivalDTO(
    val id: Int,
    val idCuarto: Int,
    val dorsalJugador: Int,
    val posX: Float,
    val posY: Float
)

@Serializable
data class AddPlayerRival(
    val posX: Float,
    val posY: Float
)

@Serializable
data class CrearAlineacionRivalRequest(val idCuarto: Int, val dorsalJugador: Int, val posX: Float, val posY: Float)

object AlineacionRivalApi {
    private lateinit var client: HttpClient

    fun initialize(client: HttpClient) {
        this.client = client
    }

    suspend fun updatePlayerRival(id: Int, posX: Float, posY: Float): Boolean {
        val url ="${getApiBaseUrl()}/alineacion-rival/$id"

        return try {
            val response: HttpResponse = client.put(url) {
                contentType(ContentType.Application.Json)
                setBody(AddPlayerAlineacion(posX, posY))
            }

            if (response.status == HttpStatusCode.OK){
                val status = true
                status
            }else {
                false
            }
        } catch (e: Exception) {
            println("Error añadiendo jugador: ${e.message}")
            false
        }
    }

    suspend fun getAlineaciones(idCuarto: Int): List<AlineacionRivalDTO> {
        return client.get("${getApiBaseUrl()}/alineacion-rival/cuarto/$idCuarto").body()
    }

    suspend fun crearAlineacion(request: CrearAlineacionRivalRequest): AlineacionRivalDTO {
        return client.post("${getApiBaseUrl()}/alineacion-rival") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun eliminarAlineacion(id: Int): Boolean {
        val response = client.delete("${getApiBaseUrl()}/alineacion/rival/$id")
        return response.status == HttpStatusCode.OK
    }
}
