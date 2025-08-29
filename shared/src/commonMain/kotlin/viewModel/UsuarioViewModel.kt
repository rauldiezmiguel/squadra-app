package viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import network.EstadisticasApi
import network.JugadorDTO
import network.PerfilUsuarioDTO
import network.UsuarioApi

sealed class PerfilState {
    data object Loading : PerfilState()
    data class Success(val perfil: PerfilUsuarioDTO) : PerfilState()
    data class Error(val message: String) : PerfilState()
}

sealed class ChangePasswordState {
    data object Loading : ChangePasswordState()
    data class Success(val change: Boolean) : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

class UsuarioViewModel : CommonViewModel() {
    private val _perfil = MutableStateFlow<PerfilState>(PerfilState.Loading)
    val perfil: StateFlow<PerfilState> = _perfil

    private val _changePassword = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Loading)
    val changePassword: StateFlow<ChangePasswordState> = _changePassword

    fun cargarPerfilUsuario() {
        viewModelScope.launch {
            _perfil.value = PerfilState.Loading
            try {
                val response = UsuarioApi.getPerfilUsuario()
                if (response != null) {
                    _perfil.value = PerfilState.Success(response)
                } else {
                    _perfil.value = PerfilState.Error("No se han podido obtener los datos del perfil.")
                }
            } catch (e: Exception) {
                _perfil.value = PerfilState.Error("Error desconocido: ${e.message}")
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePassword.value = ChangePasswordState.Loading
            try {
                val response = UsuarioApi.changePassword(currentPassword, newPassword)
                if (response) {
                    _changePassword.value = ChangePasswordState.Success(response)
                } else {
                    _changePassword.value = ChangePasswordState.Error("No se ha podido cambiar la contraseña.")
                }
            } catch (e: Exception) {
                _changePassword.value = ChangePasswordState.Error("Error desconocido: ${e.message}")
            }
        }
    }
}