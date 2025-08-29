package network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.setBody
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class CuartosEquipoDTO(
    val id: Int,
    val idPartido: Int,
    val numero: Int,
    val funcionamiento: String?,
    val danoRival: String?,
    val observaciones: String?
)

@Serializable
data class CuartosEquipoPartidoDTO(
    val id: Int,
    val idPartido: Int,
    val numero: Int,
    val idAlineacion: Int,
    val funcionamiento: String?,
    val danoRival: String?,
    val observaciones: String?
)

@Serializable
data class ModificarCuartosEquipoRequest(
    val funcionamiento: String?,
    val danoRival: String?,
    val observaciones: String?
)

object CuartosEquipoApi {
    private lateinit var client: HttpClient

    fun initialize(client: HttpClient) {
        this.client = client
    }

    suspend fun getCuartosEquipo(idPartido: Int): List<CuartosEquipoPartidoDTO> {
        return client.get("${getApiBaseUrl()}/cuartos-equipo/partido/$idPartido").body()
    }

    suspend fun actualizarEquipo(id: Int, funcionamiento: String?, danoRival: String?, observaciones: String?): CuartosEquipoDTO? {
        val response = client.put("${getApiBaseUrl()}/cuartos-equipo/$id") {
            contentType(ContentType.Application.Json)
            setBody(ModificarCuartosEquipoRequest(funcionamiento, danoRival, observaciones))
        }
        return if (response.status == HttpStatusCode.OK) response.body() else null
    }

    suspend fun eliminarCuartoEquipo(id: Int): Boolean {
        val response = client.delete("${getApiBaseUrl()}/cuartos-equipo/$id")
        return response.status == HttpStatusCode.OK
    }
}