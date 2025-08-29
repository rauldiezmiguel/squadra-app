package viewModel

import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.stateIn
import kotlinx.coroutines.flow.SharingStarted
import storage.TokenStorage

class IOSAuthViewModel(private val tokenStorage: TokenStorage): KMMViewModel() {
    private val vm = AuthViewModel(tokenStorage)

    val loginState = vm.loginState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), LoginState.Idle)
    val authState = vm.authState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AuthState.Loading)
    val userRole = vm.userRole.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    fun login(username: String, password: String) = vm.login(username, password)
    fun logout() = vm.logout()
    fun checkExistingSession() = vm.checkExistingSession()
}