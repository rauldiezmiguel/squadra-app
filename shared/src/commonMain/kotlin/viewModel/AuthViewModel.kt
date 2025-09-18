package viewModel

import io.ktor.client.HttpClient
import storage.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.AuthApi
import network.CreateUserRequest

// Define los diferentes estados posibles del proceso de login.
sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object NavigateToMainScreen : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val userId: Int) : AuthState()
}

sealed class CreateUserState {
    data object Loading : CreateUserState()
    data class Success(val create: Boolean) : CreateUserState()
    data class Error(val message: String) : CreateUserState()
}

/**
 * Este ViewModel administra la lógica de autenticación, sin depender de Android.
 * Recibe la implementación de TokenStorage y AuthApi para interactuar con la API de autenticación.
 */
open class AuthViewModel(private val tokenStorage: TokenStorage) : CommonViewModel() {

    // Estado interno del login
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    private val _userRole = MutableStateFlow(tokenStorage.getUserRole() ?: "")
    val userRole: StateFlow<String> = _userRole

    // Estado para indicar si se realizó logout
    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut

    // Estado para indicar si se creo correctamente el usuario
    private val _createUserState = MutableStateFlow<CreateUserState>(CreateUserState.Loading)
    val createUserState: StateFlow<CreateUserState> = _createUserState

    /**
     * Inicializa la API de autenticación con un HttpClient y el tokenStorage.
     * Se hace una única vez antes de llamar a login o logout.
     */
    fun initializeAuthApi(client: HttpClient) {
        // Inicializa AuthApi solo una vez
        AuthApi.initialize(client, tokenStorage)
    }

    /**
     * Realiza la solicitud de login y actualiza el estado en consecuencia.
     */
    fun login(username: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val loginResponse = AuthApi.login(username, password)
                if (loginResponse != null) {
                    tokenStorage.saveTokens(loginResponse.accessToken, loginResponse.refreshToken)
                    _loginState.value = LoginState.NavigateToMainScreen
                    _authState.value = AuthState.Authenticated(loginResponse.id)
                    _userRole.value = loginResponse.tipoUsuario
                } else {
                    _loginState.value = LoginState.Error("Credenciales incorrectas")
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Realiza el logout notificando al backend y actualiza el estado.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                val respuesta = AuthApi.logout() // Notifica al backend
                if (respuesta) {
                    _loginState.value = LoginState.Idle // Volver al estado inicial
                    _isLoggedOut.value = true
                    println("Sesión cerrada correctamente.")
                    _authState.value = AuthState.Unauthenticated
                    _userRole.value = ""
                }else {
                    println("La sesión no se ha podido cerrar.")
                }

            } catch (_: Exception) {
                // Ignorar errores en logout
            }
        }
    }

    /**
     * Función que permite la creación de usuarios en la base de datos.
     */
    fun createUser(nombreUsuario: String, passWrd: String, tipoUsuario: String, idClub: Int?) {
        viewModelScope.launch {
            try {
                val respuesta = AuthApi.createUser(nombreUsuario, passWrd, tipoUsuario, idClub)
                if (respuesta) {
                    _createUserState.value = CreateUserState.Success(respuesta)
                } else {
                    _createUserState.value = CreateUserState.Error("No se ha podido crear el usuario.")
                }
            } catch (e: Exception) {
                _createUserState.value = CreateUserState.Error("Error creando el usuario: ${e.message}")
            }
        }
    }

    fun checkExistingSession() {
        val token = tokenStorage.getToken()
        val userId = tokenStorage.getUserId()

        _authState.value = if (!token.isNullOrBlank() && userId != null) {
            AuthState.Authenticated(userId)
        } else {
            AuthState.Unauthenticated
        }
    }


}