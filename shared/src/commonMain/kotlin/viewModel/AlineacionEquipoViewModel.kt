package viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import network.AlineacionEquipoApi
import network.AlineacionEquipoDTO
import network.AlineacionEquipoUI
import network.CrearAlineacionEquipoRequest
import network.JugadorDTO

sealed class AlineacionEquipoState {
    data object Loading : AlineacionEquipoState()
    data class Success(val alineaciones: List<AlineacionEquipoDTO>) : AlineacionEquipoState()
    data class Error(val message: String) : AlineacionEquipoState()
}

sealed class AddPlayerState {
    data object Loading : AddPlayerState()
    data class Success(val stats: Boolean) : AddPlayerState()
    data class Error(val message: String) : AddPlayerState()
}

sealed class AlineacionPorCuartosState {
    data object Loading : AlineacionPorCuartosState()
    data class Success(val alineaciones: MutableStateFlow<Map<Int, List<AlineacionEquipoDTO>>>) : AlineacionPorCuartosState()
    data class Error(val message: String) : AlineacionPorCuartosState()
}

class AlineacionEquipoViewModel : CommonViewModel() {

    private val _alineacionEquipoState = MutableStateFlow<AlineacionEquipoState>(AlineacionEquipoState.Loading)
    val alineacionEquipoState: StateFlow<AlineacionEquipoState> = _alineacionEquipoState

    private val _alineaciones = MutableStateFlow<List<AlineacionEquipoDTO>>(emptyList())
    val alineaciones: StateFlow<List<AlineacionEquipoDTO>> = _alineaciones

    private val _addPlayer = MutableStateFlow<AddPlayerState>(AddPlayerState.Loading)
    val addPlayer: StateFlow<AddPlayerState> = _addPlayer

    // Ahora almacenas también la lista de jugadores
    private val _jugadores = MutableStateFlow<List<JugadorDTO>>(emptyList())
    val jugadores: StateFlow<List<JugadorDTO>> = _jugadores

    private val _alineacionesPorCuarto = MutableStateFlow<Map<Int, List<AlineacionEquipoDTO>>>(emptyMap())
    val alineacionesPorCuarto = _alineacionesPorCuarto.asStateFlow()

    // Expones la lista combinada para la UI
    val alineacionesUI: StateFlow<List<AlineacionEquipoUI>> = combine(
        alineaciones,
        jugadores
    ) { alineacionesList, jugadoresList ->
        alineacionesList.mapNotNull { alineacion ->
            jugadoresList.find { it.id == alineacion.idJugador }?.let { jugador ->
                AlineacionEquipoUI(
                    idAlineacion = alineacion.id,
                    idCuarto = alineacion.idCuarto,
                    jugador = jugador,
                    posX = alineacion.posX,
                    posY = alineacion.posY
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ✅ Método para actualizar la lista de jugadores desde otro ViewModel
    fun actualizarJugadores(nuevaLista: List<JugadorDTO>) {
        _jugadores.value = nuevaLista
    }


    fun updatePlayerAlineacion(id: Int, posX: Float, posY: Float) {
        viewModelScope.launch {
            try {
                val addPlayer = AlineacionEquipoApi.updatePlayerAlineacion(id, posX, posY)
                _addPlayer.value = AddPlayerState.Success(addPlayer)
            }catch (e: Exception) {
                _addPlayer.value = AddPlayerState.Error(e.message ?: "Error desconocido")
            }
        }
    }


    fun obtenerAlineaciones(idCuarto: Int) {
        _alineacionEquipoState.value = AlineacionEquipoState.Loading
        viewModelScope.launch {
            try {
                val alineaciones = AlineacionEquipoApi.getAlineaciones(idCuarto)
                // Actualizar el mapa de alineaciones por cuarto
                val currentMap = _alineacionesPorCuarto.value.toMutableMap()
                currentMap[idCuarto] = alineaciones
                _alineacionesPorCuarto.value = currentMap

                // Actualizar la lista plana para alimentar alineacionesUI
                // Por ejemplo, aplanar todas las alineaciones de todos los cuartos
                val todasAlineaciones = currentMap.values.flatten()
                _alineaciones.value = todasAlineaciones

                _alineacionEquipoState.value = AlineacionEquipoState.Success(alineaciones)
            } catch (e: Exception) {
                _alineacionEquipoState.value = AlineacionEquipoState.Error(e.message ?: "Error desconocido")
            }
        }
    }


    fun crearAlineacion(request: CrearAlineacionEquipoRequest) {
        viewModelScope.launch {
            try {
                val nueva = AlineacionEquipoApi.crearAlineacion(
                    idCuarto = request.idCuarto,
                    idJugador = request.idJugador,
                    posX = request.posX,
                    posY = request.posY
                )
                val actual = _alineaciones.value.toMutableList()
                actual.add(nueva)
                _alineaciones.value = actual
            } catch (e: Exception) {
                println("Error creando alineación: ${e.message}")
            }
        }
    }

    fun eliminarAlineacion(id: Int) {
        viewModelScope.launch {
            try {
                val eliminado = AlineacionEquipoApi.eliminarAlineacion(id)
                if (eliminado) {
                    val actual = _alineaciones.value.toMutableList().filter { it.id != id }
                    _alineaciones.value = actual
                }
            } catch (e: Exception) {
                println("Error eliminando alineación: ${e.message}")
            }
        }
    }
}
