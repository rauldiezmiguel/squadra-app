package viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import storage.TokenStorage
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Adaptador del AuthViewModel para Android. Extiende de androidx.lifecycle.ViewModel
 * para integrarse en el ciclo de vida de Android (por ejemplo, usando ViewModelProvider).
 */
class AndroidAuthViewModel(tokenStorage: TokenStorage) : ViewModel() {
    private val commonAuthViewModel = AuthViewModel(tokenStorage)

    // Exponemos el estado (por ejemplo, para colectarlo en Composables o Activities)
    val loginState get() = commonAuthViewModel.loginState
    val isLoggedOut get() = commonAuthViewModel.isLoggedOut
    val authState: StateFlow<AuthState> = commonAuthViewModel.authState
    val userRole: StateFlow<String> = commonAuthViewModel.userRole

    /**
     * Inicializa AuthApi con un HttpClient.
     * Esta función se llama desde la capa de presentación, pasando el cliente configurado para Android.
     */
    fun initializeAuthApi(client: HttpClient) {
        viewModelScope.launch {
            commonAuthViewModel.initializeAuthApi(client)
        }
    }

    /**
     * Realiza el login delegando en el ViewModel común.
     */
    fun login(username: String, password: String) {
        commonAuthViewModel.login(username, password)
    }

    /**
     * Realiza el logout delegando en el ViewModel común.
     */
    fun logout() {
        commonAuthViewModel.logout()
    }

    fun checkExistingSession() {
        commonAuthViewModel.checkExistingSession()
    }

    override fun onCleared() {
        // Cancela las corrutinas cuando se destruya la instancia de Android.
        commonAuthViewModel.stop()
        super.onCleared()
    }
}