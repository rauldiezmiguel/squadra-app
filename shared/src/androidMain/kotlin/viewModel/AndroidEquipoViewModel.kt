package viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import storage.TokenStorage

class AndroidEquipoViewModel(tokenStorage: TokenStorage) : ViewModel() {
    private val commonEquipoViewModel = EquipoViewModel(tokenStorage)

    val equipoState get() = commonEquipoViewModel.equipoState
    val categoriaState get() = commonEquipoViewModel.categoriaState
    val equipoByClubYTemporada: StateFlow<EquipoByClubYTemporadaState> = commonEquipoViewModel.equipoByClubYTemporada

    fun getEquiposForUser() {
        commonEquipoViewModel.getEquiposForUser()
    }

    fun getCategoriaById(id: Int) {
        commonEquipoViewModel.getCategoriaById(id)
    }

    fun getEquiposByClubByTemporada(idClub: Int, idTemporada: Int) {
        commonEquipoViewModel.getEquiposByClubByTemporada(idClub, idTemporada)
    }

    override fun onCleared() {
        commonEquipoViewModel.stop()
        super.onCleared()
    }
}