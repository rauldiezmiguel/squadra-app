package viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import network.AlineacionEquipoDTO
import network.AlineacionRivalApi
import network.AlineacionRivalDTO
import network.CrearAlineacionRivalRequest

sealed class AlineacionRivalState {
    data object Loading : AlineacionRivalState()
    data class Success(val alineaciones: List<AlineacionRivalDTO>) : AlineacionRivalState()
    data class Error(val message: String) : AlineacionRivalState()
}

sealed class AlineacionRivalPorCuartosState {
    data object Loading : AlineacionRivalPorCuartosState()
    data class Success(val alineaciones: MutableStateFlow<Map<Int, List<AlineacionRivalDTO>>>) : AlineacionRivalPorCuartosState()
    data class Error(val message: String) : AlineacionRivalPorCuartosState()
}

sealed class AddPlayerRivalState {
    data object Loading : AddPlayerRivalState()
    data class Success(val stats: Boolean) : AddPlayerRivalState()
    data class Error(val message: String) : AddPlayerRivalState()
}

class AlineacionRivalViewModel : CommonViewModel() {

    private val _alineacionRivalState = MutableStateFlow<AlineacionRivalState>(AlineacionRivalState.Loading)
    val alineacionRivalState: StateFlow<AlineacionRivalState> = _alineacionRivalState

    private val _alineaciones = MutableStateFlow<List<AlineacionRivalDTO>>(emptyList())
    val alineaciones: StateFlow<List<AlineacionRivalDTO>> = _alineaciones

    private val _alineacionesPorCuarto = MutableStateFlow<Map<Int, List<AlineacionRivalDTO>>>(emptyMap())
    val alineacionesPorCuarto = _alineacionesPorCuarto.asStateFlow()

    private val _addPlayer = MutableStateFlow<AddPlayerRivalState>(AddPlayerRivalState.Loading)
    val addPlayer: StateFlow<AddPlayerRivalState> = _addPlayer

    fun obtenerAlineaciones(idCuarto: Int) {
        _alineacionRivalState.value = AlineacionRivalState.Loading
        viewModelScope.launch {
            try {
                val alineaciones = AlineacionRivalApi.getAlineaciones(idCuarto)

                // Actualizar el mapa de alineaciones por cuarto
                val currentMap = _alineacionesPorCuarto.value.toMutableMap()
                currentMap[idCuarto] = alineaciones
                _alineacionesPorCuarto.value = currentMap

                // Actualizar la lista plana para alimentar alineacionesUI
                // Por ejemplo, aplanar todas las alineaciones de todos los cuartos
                val todasAlineaciones = currentMap.values.flatten()
                _alineaciones.value = todasAlineaciones

                _alineacionRivalState.value = AlineacionRivalState.Success(alineaciones)
            } catch (e: Exception) {
                _alineacionRivalState.value = AlineacionRivalState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun crearAlineacion(request: CrearAlineacionRivalRequest) {
        viewModelScope.launch {
            try {
                val nueva = AlineacionRivalApi.crearAlineacion(
                    request = request
                )
                val actual = _alineaciones.value.toMutableList()
                actual.add(nueva)
                _alineaciones.value = actual
            } catch (e: Exception) {
                println("Error creando alineación rival: ${e.message}")
            }
        }
    }

    fun eliminarAlineacion(id: Int) {
        viewModelScope.launch {
            try {
                val eliminado = AlineacionRivalApi.eliminarAlineacion(id)
                if (eliminado) {
                    val actual = _alineaciones.value.toMutableList().filter { it.id != id }
                    _alineaciones.value = actual
                }
            } catch (e: Exception) {
                println("Error eliminando alineación rival: ${e.message}")
            }
        }
    }

    fun updatePlayerRival(id: Int, posX: Float, posY: Float) {
        viewModelScope.launch {
            try {
                val addPlayer = AlineacionRivalApi.updatePlayerRival(id, posX, posY)
                _addPlayer.value = AddPlayerRivalState.Success(addPlayer)
            } catch (e: Exception) {
                _addPlayer.value = AddPlayerRivalState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
