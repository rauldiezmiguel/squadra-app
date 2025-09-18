package viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class AndroidUsuarioViewModel(): ViewModel() {
    private val commonUsuarioViewModel = UsuarioViewModel()

    val perfilState: StateFlow<PerfilState> = commonUsuarioViewModel.perfil
    val changePassword: StateFlow<ChangePasswordState> = commonUsuarioViewModel.changePassword
    val usersByClub: StateFlow<UsersByClubState> = commonUsuarioViewModel.usersByClub
    val deleteUser: StateFlow<DeleteUserState> = commonUsuarioViewModel.deleteUser

    fun cargarPerfilUsuario() {
        commonUsuarioViewModel.cargarPerfilUsuario()
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        commonUsuarioViewModel.changePassword(currentPassword, newPassword)
    }

    fun getUserByClub(idClub: Int) {
        commonUsuarioViewModel.getUserByClub(idClub)
    }

    fun deleteUser(id: Int) {
        commonUsuarioViewModel.deleteUser(id)
    }
}