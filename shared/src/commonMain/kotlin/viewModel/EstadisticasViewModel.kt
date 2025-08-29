package viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.CalendarioApi
import network.CrearEstadisticasRequest
import network.EstadisticaEquipoDTO
import network.EstadisticaEquipoDetalleDTO
import network.EstadisticaPartidoDTO
import network.EstadisticasApi
import network.EstadisticasJugadorDTO
import network.EstadisticasTotalesEquipoDTO
import network.EstadisticasTotalesJugadorDTO
import network.JugadorDTO
import kotlin.math.min

/**
 * Estado genérico de operaciones en el calendario.
 */
sealed class CalendarioState {
    data object Idle : CalendarioState()
    data object Loading : CalendarioState()
    data object Success : CalendarioState()
    data class Error(val message: String) : CalendarioState()
}

sealed class EstadisticaTotalJugadorState {
    data object Loading : EstadisticaTotalJugadorState()
    data class Success(val data: EstadisticasTotalesJugadorDTO) : EstadisticaTotalJugadorState()
    data class Error(val message: String) : EstadisticaTotalJugadorState()
}

sealed class DetallePartidoState {
    data object Loading : DetallePartidoState()
    data class Success(val data: List<EstadisticaPartidoDTO>) : DetallePartidoState()
    data class Error(val message: String) : DetallePartidoState()
}

sealed class EstadisticaJugadorDetalleState {
    data object Loading : EstadisticaJugadorDetalleState()
    data class Success(val data: List<EstadisticaPartidoDTO>) : EstadisticaJugadorDetalleState()
    data class Error(val message: String) : EstadisticaJugadorDetalleState()
}

sealed class TeamDetalleState {
    data object Loading : TeamDetalleState()
    data class Success(val data: EstadisticaEquipoDTO) : TeamDetalleState()
    data class Error(val message: String) : TeamDetalleState()
}

sealed class EstadisticasTotalesEquipoState {
    data object Loading : EstadisticasTotalesEquipoState()
    data class Success(val data: EstadisticasTotalesEquipoDTO) : EstadisticasTotalesEquipoState()
    data class Error(val message: String) : EstadisticasTotalesEquipoState()
}

sealed class EstadisticasJugadorPartidoState {
    data object Loading : EstadisticasJugadorPartidoState()
    data class Succes(val data: List<EstadisticasJugadorDTO>) : EstadisticasJugadorPartidoState()
    data class Error(val message: String) : EstadisticasJugadorPartidoState()
}

class EstadisticasViewModel() : CommonViewModel() {
    private val _crearEstadisticaState = MutableStateFlow<CalendarioState>(CalendarioState.Idle)
    val crearEstadisticaState: StateFlow<CalendarioState> = _crearEstadisticaState

    private val _estadisticasTotalJugadorState = MutableStateFlow<EstadisticaTotalJugadorState>(EstadisticaTotalJugadorState.Loading)
    val estadisticasTotalJugadorState: StateFlow<EstadisticaTotalJugadorState> = _estadisticasTotalJugadorState

    private val _detalleState = MutableStateFlow<DetallePartidoState>(DetallePartidoState.Loading)
    val detalleState: StateFlow<DetallePartidoState> = _detalleState

    private val _jugadorDetalleState = MutableStateFlow<EstadisticaJugadorDetalleState>(EstadisticaJugadorDetalleState.Loading)
    val jugadorDetalleState: StateFlow<EstadisticaJugadorDetalleState> = _jugadorDetalleState

    private val _teamDetalleState = MutableStateFlow<TeamDetalleState>(TeamDetalleState.Loading)
    val teamDetalleState: StateFlow<TeamDetalleState> = _teamDetalleState

    private val _estadisticasTotalesEquipoState = MutableStateFlow<EstadisticasTotalesEquipoState>(EstadisticasTotalesEquipoState.Loading)
    val estadisticasTotalesEquipoState: StateFlow<EstadisticasTotalesEquipoState> = _estadisticasTotalesEquipoState

    private val _estadisticasJugadorPartido = MutableStateFlow<EstadisticasJugadorPartidoState>(EstadisticasJugadorPartidoState.Loading)
    val estadisticasJugadorPartido: StateFlow<EstadisticasJugadorPartidoState> = _estadisticasJugadorPartido

    /**
     * Llama a la API para crear una estadística y actualiza el estado.
     */
    fun crearEstadisticas(request: CrearEstadisticasRequest) {
        _crearEstadisticaState.value = CalendarioState.Loading
        // Lanzamos en un scope de ViewModel
        viewModelScope.launch {
            try {
                val ok = EstadisticasApi.crearEstadistica(request)
                if (ok) {
                    _crearEstadisticaState.value = CalendarioState.Success
                } else {
                    _crearEstadisticaState.value = CalendarioState.Error("No se pudo guardar la estadística")
                }
            } catch (e: Exception) {
                _crearEstadisticaState.value = CalendarioState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getEstadisticasTotalesJugador(idJugador: Int, idTemporada: Int) {
        viewModelScope.launch {
            _estadisticasTotalJugadorState.value = EstadisticaTotalJugadorState.Loading
            try {
                val lista = EstadisticasApi.getEstadisticasTotalesJugador(idJugador, idTemporada)
                if (lista.isNotEmpty()) {
                    // Si el endpoint devuelve lista, tomamos el primero
                    _estadisticasTotalJugadorState.value = EstadisticaTotalJugadorState.Success(lista.first())
                } else {
                    _estadisticasTotalJugadorState.value = EstadisticaTotalJugadorState.Error("Sin estadísticas para esta temporada")
                }
            } catch (e: Exception) {
                _estadisticasTotalJugadorState.value = EstadisticaTotalJugadorState.Error("Error: ${e.message}")
            }
        }
    }

    fun getEstadisticasByJugadorByPartido(idJugador: Int, idPartido: Int) {
        viewModelScope.launch {
            _estadisticasJugadorPartido.value = EstadisticasJugadorPartidoState.Loading
            try {
                val estadisticasPartido = EstadisticasApi.getEstadisticasByJugadorByPartido(idJugador, idPartido)
                if (estadisticasPartido.isNotEmpty()) {
                    _estadisticasJugadorPartido.value = EstadisticasJugadorPartidoState.Succes(estadisticasPartido)
                } else {
                    _estadisticasJugadorPartido.value = EstadisticasJugadorPartidoState.Error("Sin estadísticas de este jugador con ID: $idJugador para el partido con este ID: $idPartido")
                }
            } catch (e: Exception) {
                _estadisticasJugadorPartido.value = EstadisticasJugadorPartidoState.Error("Error: ${e.message}")
            }
        }
    }

    fun getEstadisticasDeJugadoresParaPartido(jugadores: List<JugadorDTO>, idPartido: Int) {
        viewModelScope.launch {
            _estadisticasJugadorPartido.value = EstadisticasJugadorPartidoState.Loading
            try {
                val estadisticas = jugadores.flatMap { jugador ->
                    EstadisticasApi.getEstadisticasByJugadorByPartido(jugador.id, idPartido)
                }
                if (estadisticas.isNotEmpty()) {
                    _estadisticasJugadorPartido.value = EstadisticasJugadorPartidoState.Succes(estadisticas)
                } else {
                    _estadisticasJugadorPartido.value = EstadisticasJugadorPartidoState.Error("No hay estadísticas para este partido.")
                }
            } catch (e: Exception) {
                _estadisticasJugadorPartido.value = EstadisticasJugadorPartidoState.Error("Error: ${e.message}")
            }
        }
    }

    fun cargarEstadisticaPartidos(
        idEquipo: Int,
        idTemporada: Int,
        nomEstadistica: String
    ) {
        viewModelScope.launch {
            _detalleState.value = DetallePartidoState.Loading

            try {
                val listaDto = EstadisticasApi.getDetalleEstadisticaEquipo(idEquipo, idTemporada, nomEstadistica)
                val data = listaDto.map {
                    EstadisticaPartidoDTO(
                        idPartido = it.idPartido,
                        valorEstadistica = it.valorEstadistica,
                        nomEstadistica = nomEstadistica,
                        nombreRival = it.nombreRival
                    )
                }
                _detalleState.value = DetallePartidoState.Success(data)
            } catch (e: Exception) {
                _detalleState.value = DetallePartidoState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Carga la serie histórica de `nomEstadistica` para un jugador
     */
    fun cargarEstadisticaJugadorPartidos(
        idJugador: Int,
        idEquipo: Int,
        idTemporada: Int,
        nomEstadistica: String
    ) {
        viewModelScope.launch {
            _jugadorDetalleState.value = EstadisticaJugadorDetalleState.Loading
            try {
                val listaDto = EstadisticasApi.getDetalleEstadisticaJugador(
                    idJugador, idEquipo, idTemporada, nomEstadistica
                )
                if (listaDto.isEmpty()) {
                    _jugadorDetalleState.value =
                        EstadisticaJugadorDetalleState.Error("Sin datos de $nomEstadistica")
                } else {
                    _jugadorDetalleState.value = EstadisticaJugadorDetalleState.Success(listaDto)
                }
            } catch (e: Exception) {
                _jugadorDetalleState.value =
                    EstadisticaJugadorDetalleState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Carga los totales agregados de una estadística para el equipo y temporada.
     */
    fun cargarDetalleEstadisticaEquipo(
        idEquipo: Int,
        idTemporada: Int,
        nomEstadistica: String
    ) {
        viewModelScope.launch {
            _teamDetalleState.value = TeamDetalleState.Loading
            try {
                val dto = EstadisticasApi.getDetalleEstadisticaEquipoAgregado(
                    idEquipo = idEquipo,
                    idTemporada = idTemporada,
                    nomEstadistica = nomEstadistica
                )
                if (dto == null || dto.detallesPorJugador.isEmpty()) {
                    _teamDetalleState.value = TeamDetalleState.Error("Sin datos de $nomEstadistica")
                } else {
                    _teamDetalleState.value = TeamDetalleState.Success(dto) // ✅ pasa objeto, no lista
                }
            } catch (e: Exception) {
                _teamDetalleState.value = TeamDetalleState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getEstadisticasTotalesEquipo(idEquipo: Int, idTemporada: Int) {
        viewModelScope.launch {
            _estadisticasTotalesEquipoState.value = EstadisticasTotalesEquipoState.Loading

            try {
                // Realizamos la llamada a la API para obtener todas las estadísticas del equipo
                val estadisticas = EstadisticasApi.getEstadisticasEquipoByTemporada(idEquipo, idTemporada)

                if (estadisticas.isNotEmpty()) {
                    // Mapeamos los datos y acumulamos las estadísticas
                    val goles = estadisticas.firstOrNull()?.golesTotales ?: 0
                    val asistencias = estadisticas.firstOrNull()?.asistenciasTotales ?: 0
                    val minutosTotales = estadisticas.firstOrNull()?.minutosTotales ?: 0
                    val partidosTotales = estadisticas.firstOrNull()?.partidosTotales ?: 0
                    val tarjetasAmarillas = estadisticas.firstOrNull()?.tarjetasAmarillasTotales ?: 0
                    val tarjetasRojas = estadisticas.firstOrNull()?.tarjetasRojasTotales ?: 0

                    // Creamos el DTO y actualizamos el estado
                    val estadisticasTotalesEquipo = EstadisticasTotalesEquipoDTO(
                        idEquipo = idEquipo,
                        idTemporada = idTemporada,
                        golesTotales = goles,
                        asistenciasTotales = asistencias,
                        minutosTotales = minutosTotales,
                        partidosTotales = partidosTotales,
                        tarjetasAmarillasTotales = tarjetasAmarillas,
                        tarjetasRojasTotales = tarjetasRojas
                    )

                    _estadisticasTotalesEquipoState.value = EstadisticasTotalesEquipoState.Success(estadisticasTotalesEquipo)
                } else {
                    _estadisticasTotalesEquipoState.value = EstadisticasTotalesEquipoState.Error("No se encontraron estadísticas para este equipo y temporada.")
                }

            } catch (e: Exception) {
                _estadisticasTotalesEquipoState.value = EstadisticasTotalesEquipoState.Error("Error al obtener las estadísticas del equipo: ${e.message}")
                println("Error al obtener estadísticas del equipo: ${e.message}")
            }
        }
    }
}