package viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.CuartosEquipoApi
import network.CuartosEquipoDTO
import network.CuartosEquipoPartidoDTO
import network.ModificarCuartosEquipoRequest

sealed class CuartoEquipoState {
    data object Loading: CuartoEquipoState()
    data class Success(val cuarto: List<CuartosEquipoPartidoDTO>) : CuartoEquipoState()
    data class Error(val message: String) : CuartoEquipoState()
}

sealed class CuartoActualizadoEquipoState {
    data object Loading: CuartoActualizadoEquipoState()
    data class Success(val cuarto: CuartosEquipoDTO) : CuartoActualizadoEquipoState()
    data class Error(val message: String) : CuartoActualizadoEquipoState()
}

class CuartosEquipoViewModel() : CommonViewModel() {
    private val _cuartoEquipoState = MutableStateFlow<CuartoEquipoState>(CuartoEquipoState.Loading)
    val cuartoEquipoState: StateFlow<CuartoEquipoState> = _cuartoEquipoState

    private val _cuartosEquipo = MutableStateFlow<List<CuartosEquipoPartidoDTO>>(emptyList())
    val cuartosEquipo: StateFlow<List<CuartosEquipoPartidoDTO>> = _cuartosEquipo

    private val _cuartoActualizadoEquipoState = MutableStateFlow<CuartoActualizadoEquipoState>(CuartoActualizadoEquipoState.Loading)
    val cuartoActulizadoEquipoState: StateFlow<CuartoActualizadoEquipoState> = _cuartoActualizadoEquipoState

    fun obtenerCuartosEquipoPorPartido(idPartido: Int) {
        _cuartoEquipoState.value = CuartoEquipoState.Loading
        viewModelScope.launch {
            try {
                println("Obteniendo cuartos para el partido $idPartido")
                val cuartos = CuartosEquipoApi.getCuartosEquipo(idPartido)
                println("Cuartos obtenidos: $cuartos")

                if(cuartos != null) {
                    _cuartoEquipoState.value = CuartoEquipoState.Success(cuartos)
                    _cuartosEquipo.value = cuartos
                } else {
                    _cuartoEquipoState.value = CuartoEquipoState.Error("No se encontraron ningún cuarto para este partido")
                }
            } catch (e: Exception) {
                _cuartoEquipoState.value = CuartoEquipoState.Error(e.message ?: "Error desconocido")
                println(e.message)
            }
        }
    }

    fun actualizarCuartoEquipo(id: Int, request: ModificarCuartosEquipoRequest) {
        _cuartoEquipoState.value = CuartoEquipoState.Loading
        viewModelScope.launch {
            try {
                println("Actualizando cuarto equipo con id $id...")
                val cuartoActualizado = CuartosEquipoApi.actualizarEquipo(
                    id = id,
                    funcionamiento = request.funcionamiento,
                    danoRival = request.danoRival,
                    observaciones = request.observaciones
                )

                if (cuartoActualizado != null) {
                    println("Cuarto actualizado: $cuartoActualizado")
                    _cuartoActualizadoEquipoState.value = CuartoActualizadoEquipoState.Success(cuartoActualizado)
                }else{
                    _cuartoActualizadoEquipoState.value = CuartoActualizadoEquipoState.Error("Error al actualizar el cuarto")
                }
            } catch (e: Exception) {
                val err = e.message ?: "Error desconocido"
                _cuartoActualizadoEquipoState.value = CuartoActualizadoEquipoState.Error(err)
            }
        }
    }
}