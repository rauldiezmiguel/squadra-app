package network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import repository.EquipoRepository
import storage.TokenStorage

expect fun getHttpEngine(): HttpClientEngine
expect fun getApiBaseUrl(): String

@Serializable
data class LoginRequest(val nombreUsuario: String, val passWrd: String)

@Serializable
data class LoginResponse(val id: Int, val accessToken: String, val refreshToken: String, val tipoUsuario: String, val idClub: Int)

@Serializable
data class RefreshTokenRequest(val refresh_token: String)

@Serializable
data class RefreshTokenResponse(val new_access_token: String)

@Serializable
data class LogoutRequest(val userId: Int)

object AuthApi {

    private lateinit var client: HttpClient
    private lateinit var tokenStorage: TokenStorage

    fun initialize(client: HttpClient, tokenStorage: TokenStorage) {
        this.client = client
        this.tokenStorage = tokenStorage
    }

    suspend fun login(username: String, password: String): LoginResponse? {
        return try {
            val response: HttpResponse = client.post("${getApiBaseUrl()}/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }

            if (response.status == HttpStatusCode.OK) {
                val loginResponse = response.body<LoginResponse>()
                tokenStorage.saveTokensAndData(loginResponse.accessToken, loginResponse.refreshToken, loginResponse.id, loginResponse.tipoUsuario, loginResponse.idClub)
                loginResponse
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error en login: ${e.message}")
            null
        }
    }

    suspend fun logout() : Boolean {
        return try {
            val response: HttpResponse = client.post("${getApiBaseUrl()}/auth/logout") {
                contentType(ContentType.Application.Json)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    tokenStorage.clearTokens()
                    EquipoRepository.clear()
                    closeClient()
                    println("Sesión cerrada correctamente")
                    true
                }
                HttpStatusCode.Unauthorized -> {
                    println("No estás autorizado")
                    false
                }
                else -> {
                    println("No se ha podido cerrar la sesión.")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error en logout: ${e.message}")
            false
        }
    }

    private fun closeClient() {
        client.close()
    }

    suspend fun getProtectedData(): String {
        return try {
            client.get("${getApiBaseUrl()}/protected-data").body()
        } catch (e: Exception) {
            println("Error obteniendo datos protegidos: ${e.message}")
            "Error al obtener datos"
        }
    }
}

// Inicialización correcta del HttpClient con Auth instalado
fun createHttpClient(tokenStorage: TokenStorage): HttpClient {
    return HttpClient(getHttpEngine()) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }

        // ✅ AÑADIR TIMEOUT
        install(HttpTimeout) {
            requestTimeoutMillis = 60000 // 60 segundos
            connectTimeoutMillis = 60000
            socketTimeoutMillis = 60000
        }


        install(HttpRequestRetry) {
            retryOnException(maxRetries = 3)
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }


        install(Auth) {
            bearer {
                loadTokens {
                    val accessToken = tokenStorage.getToken()
                    accessToken?.let { BearerTokens(it, tokenStorage.getRefreshToken() ?: "")  }
                }
                refreshTokens {
                    val refreshToken = tokenStorage.getRefreshToken()
                    if (refreshToken != null) {
                        try {
                            val response: RefreshTokenResponse = client.post("${getApiBaseUrl()}/auth/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody(RefreshTokenRequest(refreshToken))
                            }.body()

                            tokenStorage.saveTokens(response.new_access_token, refreshToken)
                            BearerTokens(response.new_access_token, refreshToken)
                        } catch (e: Exception) {
                            println("Error refrescando token: ${e.message}")
                            tokenStorage.clearTokens()
                            null
                        }
                    } else {
                        null
                    }
                }
            }
        }
    }
}

// Uso en la aplicación
fun setupAuthApi(tokenStorage: TokenStorage) {
    val httpClient = createHttpClient(tokenStorage)
    AuthApi.initialize(httpClient, tokenStorage)
    EquipoApi.initialize(httpClient, tokenStorage)
    CalendarioApi.initialize(httpClient, tokenStorage)
    JugadorApi.initialize(httpClient, tokenStorage)
    EstadisticasApi.initialize(httpClient,tokenStorage)
    AsistenciaEntrenamientosApi.initialize(httpClient, tokenStorage)
    EvaluacionApi.initialize(httpClient, tokenStorage)
    UsuarioApi.initialize(httpClient, tokenStorage)
    CuartosEquipoApi.initialize(httpClient)
    CuartosRivalApi.initialize(httpClient)
    AlineacionEquipoApi.initialize(httpClient)
    AlineacionRivalApi.initialize(httpClient)
}