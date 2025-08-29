package network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.setBody
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class AlineacionEquipoDTO(
    val id: Int,
    val idCuarto: Int,
    val idJugador: Int,
    val posX: Float,
    val posY: Float
)

@Serializable
data class AlineacionEquipoUI(
    val idAlineacion: Int,
    val idCuarto: Int,
    val jugador: JugadorDTO,
    val posX: Float,
    val posY: Float
)

@Serializable
data class CrearAlineacionEquipoRequest(val idCuarto: Int, val idJugador: Int, val posX: Float, val posY: Float)

@Serializable
data class AddPlayerAlineacion(val posx: Float, val posY: Float)

object AlineacionEquipoApi {
    private lateinit var client: HttpClient

    fun initialize(client: HttpClient) {
        this.client = client
    }

    suspend fun updatePlayerAlineacion(id: Int, posX: Float, posY: Float): Boolean {
        val url ="${getApiBaseUrl()}/alineacion-equipo/$id"

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
            println("Error actualizando jugador: ${e.message}")
            false
        }
    }

    suspend fun getAlineaciones(idCuarto: Int): List<AlineacionEquipoDTO> {
        return client.get("${getApiBaseUrl()}/alineacion-equipo/cuarto/$idCuarto").body()
    }

    suspend fun crearAlineacion(idCuarto: Int, idJugador: Int, posX: Float, posY: Float): AlineacionEquipoDTO {
        return client.post("${getApiBaseUrl()}/alineacion-equipo") {
            contentType(ContentType.Application.Json)
            setBody(CrearAlineacionEquipoRequest(idCuarto, idJugador, posX, posY))
        }.body()
    }

    suspend fun eliminarAlineacion(id: Int): Boolean {
        val response = client.delete("${getApiBaseUrl()}/alineacion/equipo/$id")
        return response.status == HttpStatusCode.OK
    }

}
