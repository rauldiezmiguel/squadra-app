package viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.TemporadaDTO
import network.TemporadasApi
import network.UsuarioDTO

sealed class AllTemporadasState {
    data object Loading : AllTemporadasState()
    data class Success(val temporadas: List<TemporadaDTO>) : AllTemporadasState()
    data class Error(val message: String) : AllTemporadasState()
}

class TemporadasViewModel : CommonViewModel() {
    private val _allTemporadas = MutableStateFlow<AllTemporadasState>(AllTemporadasState.Loading)
    val allTemporadas: StateFlow<AllTemporadasState> = _allTemporadas

    fun getAllTemporadas() {
        viewModelScope.launch {
            _allTemporadas.value = AllTemporadasState.Loading
            try {
                val response = TemporadasApi.getAllTemporadas()
                if (response.isNotEmpty()) {
                    _allTemporadas.value = AllTemporadasState.Success(response)
                } else {
                    _allTemporadas.value = AllTemporadasState.Error("Error obteniendo las temporadas.")
                }
            } catch (e: Exception) {
                _allTemporadas.value = AllTemporadasState.Error("Error desconocido: ${e.message}")
            }
        }
    }
}