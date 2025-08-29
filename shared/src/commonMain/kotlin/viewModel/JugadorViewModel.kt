package viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.CrearJugadorRequest
import network.EventoCalendario
import network.FichaJugadorDTO
import network.FichaJugadorRequest
import network.JugadorApi
import network.JugadorDTO
import repository.EquipoRepository
import repository.JugadorRepository
import storage.TokenStorage

sealed class JugadorState {
    data object Idle : JugadorState()
    data object Loading : JugadorState()
    data class Success(val jugadores: List<JugadorDTO>) : JugadorState()
    data class Error(val message: String) : JugadorState()
}

sealed class FichaJugadorState {
    data object Loading : FichaJugadorState()
    data class Success(val jugadores: FichaJugadorDTO) : FichaJugadorState()
    data class Error(val message: String) : FichaJugadorState()
}

sealed class FichasJugadoresState {
    data object Loading : FichasJugadoresState()
    data class Success(val jugadores: List<FichaJugadorDTO>) : FichasJugadoresState()
    data class Error(val message: String) : FichasJugadoresState()
}

sealed class CrearFichaJugadorState {
    data object Idle : CrearFichaJugadorState()
    data object Loading : CrearFichaJugadorState()
    data class Success(val message: String) : CrearFichaJugadorState()
    data class Error(val message: String) : CrearFichaJugadorState()
}

class JugadorViewModel (private val tokenStorage: TokenStorage): CommonViewModel() {
    private val _jugadorState = MutableStateFlow<JugadorState>(JugadorState.Loading)
    val jugadorState: StateFlow<JugadorState> = _jugadorState

    private val _fichaJugadorState = MutableStateFlow<FichaJugadorState>(FichaJugadorState.Loading)
    val fichaJugadorState: StateFlow<FichaJugadorState> = _fichaJugadorState

    private val _fichasJugadoresState = MutableStateFlow<FichasJugadoresState>(FichasJugadoresState.Loading)
    val fichasJugadoresState: StateFlow<FichasJugadoresState> = _fichasJugadoresState

    private val _crearFichaJugadorState = MutableStateFlow<CrearFichaJugadorState>(CrearFichaJugadorState.Loading)
    val crearFichaJugadorState: StateFlow<CrearFichaJugadorState> = _crearFichaJugadorState

    private val _jugadorSeleccionado = MutableStateFlow<JugadorDTO?>(null)
    val jugadorSeleccionado: StateFlow<JugadorDTO?> = _jugadorSeleccionado

    private val _lastUpdated = MutableStateFlow<String?>(null)
    val lastUpdated: StateFlow<String?> = _lastUpdated

    fun seleccionarJugador(jugador: JugadorDTO) {
        _jugadorSeleccionado.value = jugador
    }

    fun getJugadoresPorEquipo(idEquipo: Int) {

        _jugadorState.value = JugadorState.Loading

        viewModelScope.launch {
            try {
                println("Obteniendo jugadores para equipo $idEquipo...")
                val jugadores = JugadorApi.getJugadoresPorEquipo(idEquipo)
                println("Jugadores obtenidos: $jugadores")

                if(jugadores != null) {
                    JugadorRepository.saveJugadores(idEquipo, jugadores)
                    _jugadorState.value = JugadorState.Success(jugadores)
                } else {
                    println("No se encontraron jugadores.")
                    _jugadorState.value = JugadorState.Error("No se encontraron jugadores")
                }
            } catch (e: Exception) {
                _jugadorState.value = JugadorState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getJugadoresPorId(idJugador: Int) {
        viewModelScope.launch {
            try {
                val jugador = JugadorApi.getJugadoresPorId(idJugador)

                if(jugador != null) {
                    println("Jugador obtenido: $jugador")
                    _jugadorSeleccionado.value = jugador
                } else {
                    println("No se encontro al jugador.")
                    _jugadorSeleccionado.value = null
                }
            } catch (e: Exception) {
                println(e.message ?: "Error desconocido")
            }
        }
    }

    fun crearJugador(request: CrearJugadorRequest) {
        _jugadorState.value = JugadorState.Loading
        viewModelScope.launch {
            try {
                val nuevoDto = JugadorApi.crearJugador(
                    nombreJugador = request.nombre,
                    dorsal        = request.dorsal,
                    posicion      = request.posicion,
                    idEquipo      = request.idEquipo
                )
                if (nuevoDto != null) {
                    // Actualizamos caché y estado
                    refreshJugadores(request.idEquipo)
                } else {
                    _jugadorState.value = JugadorState.Error("No se pudo crear el jugador")
                }
            } catch (e: Exception) {
                _jugadorState.value = JugadorState.Error(e.message ?: "Error al crear jugador")
            }
        }
    }

    private fun refreshJugadores(idEquipo: Int) {
        JugadorRepository.clearEquipo(idEquipo)
        getJugadoresPorEquipo(idEquipo)
    }

    fun eliminarJugador(idJugador: Int, idEquipo: Int) {
        _jugadorState.value = JugadorState.Loading
        viewModelScope.launch {
            try {
                val ok = JugadorApi.eliminarJugador(idJugador)
                if (ok) {
                    _jugadorSeleccionado.value = null
                    refreshJugadores(idEquipo)
                } else {
                    _jugadorState.value = JugadorState.Error("No se pudo eliminar el jugador")
                }
            } catch (e: Exception) {
                _jugadorState.value = JugadorState.Error(e.message ?: "Error al eliminar jugador")
            }
        }
    }

    fun actualizarJugador(idJugador: Int, dorsal: Int, posicion: String) {

        viewModelScope.launch {
            try {
                val ok = JugadorApi.actualizarJugador(idJugador, dorsal, posicion)
                if (ok) {
                    println("Jugador Actualizado correctamente")
                } else {
                    println("El jugador no se ha podido actualizar")
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    fun crearFichaJugador(request: FichaJugadorRequest) {
        _crearFichaJugadorState.value = CrearFichaJugadorState.Loading

        viewModelScope.launch {
            try {
                val nuevaFichaJugador = JugadorApi.crearFichaJugador(
                    idJugador = request.idJugador,
                    idEquipo = request.idEquipo,
                    piernaHabil = request.piernaHabil,
                    caracteristicasFisicas = request.caracteristicasFisicas,
                    caracteristicasTacticas = request.caracteristicasTacticas,
                    caracteristicasTecnicas = request.caracteristicasTecnicas,
                    conductaEntrenamiento = request.conductaEntrenamiento,
                    conductaConCompañeros = request.conductaConCompañeros,
                    observacionFinal = request.observacionFinal
                )

                if (nuevaFichaJugador != null) {
                    _crearFichaJugadorState.value = CrearFichaJugadorState.Success("Ficha del jugador creada correctamente")
                } else {
                    _crearFichaJugadorState.value = CrearFichaJugadorState.Error("No se ha podido crear la ficha del jugador")
                }

             }catch (e: Exception) {
                _fichaJugadorState.value = FichaJugadorState.Error(e.message ?: "No se pudo crear el jugador")
            }
        }
    }

    fun eliminarFichaJugador(idFichaJugador: Int) {
        _crearFichaJugadorState.value = CrearFichaJugadorState.Loading

        viewModelScope.launch {
            try {
                val ok = JugadorApi.eliminarFichaJugador(idFichaJugador)
                if (ok) {
                    _crearFichaJugadorState.value = CrearFichaJugadorState.Success("Ficha del jugador eliminada correctamente")
                } else {
                    _crearFichaJugadorState.value = CrearFichaJugadorState.Error("No se pudo eliminar la ficha del jugador")
                }
            }catch (e: Exception) {
                _crearFichaJugadorState.value = CrearFichaJugadorState.Error(e.message ?: "Error al eliminar ficha del jugador")
            }
        }
    }

    fun obtenerFichaDeJugador(idJugador: Int) {
        _fichaJugadorState.value = FichaJugadorState.Loading

        viewModelScope.launch {
            try {
                println("Obteniendo la ficha del jugador por su ID $idJugador...")
                val fichaJugador = JugadorApi.obtenerFichaDeJugador(idJugador)
                println("Ficha jugador obtenido: $fichaJugador")

                if(fichaJugador != null) {
                    _fichaJugadorState.value = FichaJugadorState.Success(fichaJugador)
                } else {
                    println("No se encontraron jugadores.")
                    _fichaJugadorState.value = FichaJugadorState.Error("No se encontraron jugadores")
                }
            } catch (e: Exception) {
                _fichaJugadorState.value = FichaJugadorState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun obtenerFichaDeJugadorPorEquipo(idEquipo: Int) {
        _fichasJugadoresState.value = FichasJugadoresState.Loading

        viewModelScope.launch {
            try {
                println("Obteniendo la ficha de los jugadores de un equipo por su ID $idEquipo...")
                val fichasJugadores = JugadorApi.obtenerFichaDeJugadoresPorEquipo(idEquipo)
                println("Ficha jugador obtenido: $fichasJugadores")

                _fichasJugadoresState.value = FichasJugadoresState.Success(fichasJugadores)
            } catch (e: Exception) {
                _fichasJugadoresState.value = FichasJugadoresState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Función para obtener la última fecha de actualización
    fun getLastUpdatedPorEquipo(idEquipo: Int) {
        viewModelScope.launch {
            val updated = JugadorApi.getLastUpdated(idEquipo)
            _lastUpdated.value = updated
        }
    }
}