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
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import storage.TokenStorage

@Serializable
data class AsistenciaEntrenamientoDTO(
    val id: Int,
    val idEntrenamiento: Int,
    val idJugador: Int,
    val nombreJugador: String,
    val asistio: Boolean,
    val fecha: LocalDate,
    val motivoInasistencia: String?
)

object AsistenciaEntrenamientosApi{
    private lateinit var client: HttpClient
    private lateinit var tokenStorage: TokenStorage

    fun initialize(client: HttpClient, tokenStorage: TokenStorage) {
        this.client = client
        this.tokenStorage = tokenStorage
    }

    suspend fun getAsistencias(idEntrenamiento: Int): List<AsistenciaEntrenamientoDTO> {
        return client.get("${getApiBaseUrl()}/asistencia-entrenamientos/$idEntrenamiento").body()
    }

    suspend fun saveAsistencias(lista: List<AsistenciaEntrenamientoDTO>) {
        val response: HttpResponse = client.post("${getApiBaseUrl()}/asistencia-entrenamientos/${lista.first().idEntrenamiento}") {
            contentType(ContentType.Application.Json)
            setBody(lista)
        }
        if (response.status == HttpStatusCode.OK){
            println("Lista asistencia actualizada.")
        } else {
            println("No se ha podido actualizar la lista.")
        }
    }

    suspend fun getAsistenciasPorJugador(idJugador: Int): List<AsistenciaEntrenamientoDTO> {
        return client.get("${getApiBaseUrl()}/asistencia-entrenamientos/jugador/$idJugador").body()
    }
}