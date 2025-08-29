package network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.setBody
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class CuartosRivalDTO(
    val id: Int,
    val idPartido: Int,
    val numero: Int,
    val analisisRival: String?,
    val observaciones: String?
)

@Serializable
data class CuartosRivalPartidoDTO(
    val id: Int,
    val idPartido: Int,
    val numero: Int,
    val idAlineacion: Int, // Si aplica en tu modelo
    val analisisRival: String?,
    val observaciones: String?
)

@Serializable
data class ModificarCuartosRivalRequest(
    val analisisRival: String?,
    val observaciones: String?
)

object CuartosRivalApi {
    private lateinit var client: HttpClient

    fun initialize(client: HttpClient) {
        this.client = client
    }

    suspend fun getCuartosRival(idPartido: Int): List<CuartosRivalPartidoDTO> {
        return client.get("${getApiBaseUrl()}/cuartos-rival/partido/$idPartido").body()
    }

    suspend fun actualizarRival(id: Int, analisisRival: String?, observaciones: String?): CuartosRivalDTO? {
        val response = client.put("${getApiBaseUrl()}/cuartos-rival/$id") {
            contentType(ContentType.Application.Json)
            setBody(ModificarCuartosRivalRequest(analisisRival, observaciones))
        }
        return if (response.status == HttpStatusCode.OK) response.body() else null
    }

    suspend fun eliminarCuartoRival(id: Int): Boolean {
        val response = client.delete("${getApiBaseUrl()}/cuartos-rival/$id")
        return response.status == HttpStatusCode.OK
    }
}
