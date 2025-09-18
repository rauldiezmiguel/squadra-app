package viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import network.EstadisticasApi
import network.JugadorDTO
import network.PerfilUsuarioDTO
import network.UsuarioApi
import network.UsuarioDTO

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

sealed class UsersByClubState {
    data object Loading : UsersByClubState()
    data class Success(val users: List<UsuarioDTO>) : UsersByClubState()
    data class Error(val message: String) : UsersByClubState()
}

sealed class DeleteUserState {
    data object Loading : DeleteUserState()
    data class Success(val delete: Boolean) : DeleteUserState()
    data class Error(val message: String) : DeleteUserState()
}

class UsuarioViewModel : CommonViewModel() {
    private val _perfil = MutableStateFlow<PerfilState>(PerfilState.Loading)
    val perfil: StateFlow<PerfilState> = _perfil

    private val _changePassword = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Loading)
    val changePassword: StateFlow<ChangePasswordState> = _changePassword

    private val _usersByClub = MutableStateFlow<UsersByClubState>(UsersByClubState.Loading)
    val usersByClub: StateFlow<UsersByClubState> = _usersByClub

    private val _deleteUser = MutableStateFlow<DeleteUserState>(DeleteUserState.Loading)
    val deleteUser: StateFlow<DeleteUserState> = _deleteUser

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

    fun getUserByClub(idClub: Int) {
        viewModelScope.launch {
            _usersByClub.value = UsersByClubState.Loading
            try {
                val response = UsuarioApi.getUserByClub(idClub)
                if (response.isNotEmpty()) {
                    _usersByClub.value = UsersByClubState.Success(response)
                } else {
                    _usersByClub.value = UsersByClubState.Error("Error obteniendo los usuarios del club con ID $idClub.")
                }
            } catch (e: Exception) {
                _usersByClub.value = UsersByClubState.Error("Error desconocido: ${e.message}")
            }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            _deleteUser.value = DeleteUserState.Loading
            try {
                val response = UsuarioApi.deleteUser(id)
                if (response) {
                    _deleteUser.value = DeleteUserState.Success(response)
                } else {
                    _deleteUser.value = DeleteUserState.Error("Error eliminando al usuario con ID $id.")
                }
            } catch (e: Exception) {
                _deleteUser.value = DeleteUserState.Error("Error desconocido: ${e.message}")
            }
        }
    }
}