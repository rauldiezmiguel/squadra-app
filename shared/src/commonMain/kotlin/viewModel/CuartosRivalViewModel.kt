package viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.CuartosRivalApi
import network.CuartosRivalDTO
import network.CuartosRivalPartidoDTO
import network.ModificarCuartosRivalRequest

sealed class CuartoRivalState {
    data object Loading : CuartoRivalState()
    data class Success(val cuarto: List<CuartosRivalPartidoDTO>) : CuartoRivalState()
    data class Error(val message: String) : CuartoRivalState()
}

sealed class CuartoActualizadoRivalState {
    data object Loading : CuartoActualizadoRivalState()
    data class Success(val cuarto: CuartosRivalDTO) : CuartoActualizadoRivalState()
    data class Error(val message: String) : CuartoActualizadoRivalState()
}

class CuartosRivalViewModel : CommonViewModel() {

    private val _cuartoRivalState = MutableStateFlow<CuartoRivalState>(CuartoRivalState.Loading)
    val cuartoRivalState: StateFlow<CuartoRivalState> = _cuartoRivalState

    private val _cuartosRival = MutableStateFlow<List<CuartosRivalPartidoDTO>>(emptyList())
    val cuartosRival: StateFlow<List<CuartosRivalPartidoDTO>> = _cuartosRival

    private val _cuartoActualizadoRivalState = MutableStateFlow<CuartoActualizadoRivalState>(CuartoActualizadoRivalState.Loading)
    val cuartoActualizadoRivalState: StateFlow<CuartoActualizadoRivalState> = _cuartoActualizadoRivalState

    fun obtenerCuartosRivalPorPartido(idPartido: Int) {
        _cuartoRivalState.value = CuartoRivalState.Loading
        viewModelScope.launch {
            try {
                println("Obteniendo cuartos rival para el partido $idPartido")
                val cuartos = CuartosRivalApi.getCuartosRival(idPartido)
                println("Cuartos rival obtenidos: $cuartos")

                if (cuartos != null) {
                    _cuartoRivalState.value = CuartoRivalState.Success(cuartos)
                    _cuartosRival.value = cuartos
                } else {
                    _cuartoRivalState.value = CuartoRivalState.Error("No se encontraron cuartos rival para este partido")
                }
            } catch (e: Exception) {
                val err = e.message ?: "Error desconocido"
                _cuartoRivalState.value = CuartoRivalState.Error(err)
                println(err)
            }
        }
    }

    fun actualizarCuartoRival(id: Int, request: ModificarCuartosRivalRequest) {
        _cuartoRivalState.value = CuartoRivalState.Loading
        viewModelScope.launch {
            try {
                println("Actualizando cuarto rival con id $id...")
                val cuartoActualizado = CuartosRivalApi.actualizarRival(
                    id = id,
                    analisisRival = request.analisisRival,
                    observaciones = request.observaciones
                )

                if (cuartoActualizado != null) {
                    println("Cuarto rival actualizado: $cuartoActualizado")
                    _cuartoActualizadoRivalState.value = CuartoActualizadoRivalState.Success(cuartoActualizado)
                } else {
                    _cuartoActualizadoRivalState.value = CuartoActualizadoRivalState.Error("Error al actualizar el cuarto rival")
                }
            } catch (e: Exception) {
                val err = e.message ?: "Error desconocido"
                _cuartoActualizadoRivalState.value = CuartoActualizadoRivalState.Error(err)
                println(err)
            }
        }
    }
}
