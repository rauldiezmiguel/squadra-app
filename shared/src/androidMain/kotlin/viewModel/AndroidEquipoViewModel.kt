package viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import storage.TokenStorage

class AndroidEquipoViewModel(tokenStorage: TokenStorage) : ViewModel() {
    private val commonEquipoViewModel = EquipoViewModel(tokenStorage)

    val equipoState get() = commonEquipoViewModel.equipoState
    val categoriaState get() = commonEquipoViewModel.categoriaState

    fun getEquiposForUser() {
        commonEquipoViewModel.getEquiposForUser()
    }

    fun getCategoriaById(id: Int) {
        commonEquipoViewModel.getCategoriaById(id)
    }

    override fun onCleared() {
        commonEquipoViewModel.stop()
        super.onCleared()
    }
}