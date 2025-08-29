package viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class AndroidUsuarioViewModel(): ViewModel() {
    private val commonUsuarioViewModel = UsuarioViewModel()

    val perfilState: StateFlow<PerfilState> = commonUsuarioViewModel.perfil
    val changePassword: StateFlow<ChangePasswordState> = commonUsuarioViewModel.changePassword

    fun cargarPerfilUsuario() {
        commonUsuarioViewModel.cargarPerfilUsuario()
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        commonUsuarioViewModel.changePassword(currentPassword, newPassword)
    }
}