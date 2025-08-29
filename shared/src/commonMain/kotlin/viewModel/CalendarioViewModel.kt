package viewModel

import com.kizitonwose.calendar.core.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import network.CalendarioApi
import network.EntrenamientoDTO
import network.EventoCalendario
import network.PartidoDTO
import network.ResultadoPartido
import repository.CalendarioRepository
import storage.TokenStorage

sealed class EventoState {
    data object Idle : EventoState()
    data object Loading : EventoState()
    data class Success(val eventos: List<EventoCalendario>) : EventoState()
    data class Error(val message: String) : EventoState()
}

sealed class PartidoUIState {
    data object Loading : PartidoUIState()
    data class Success(val partidos: List<PartidoDTO>) : PartidoUIState()
    data class Error(val message: String) : PartidoUIState()
}

class CalendarioViewModel(private val tokenStorage: TokenStorage) : CommonViewModel() {
    private val eventosPorMes = mutableMapOf<YearMonth, List<EventoCalendario>>()
    private val _eventoState = MutableStateFlow<EventoState>(EventoState.Idle)
    val eventoState: StateFlow<EventoState> = _eventoState

    private val _entrenamientoSeleccionado = MutableStateFlow<EventoCalendario.Entrenamiento?>(null)
    val entrenamientoSeleccionado: StateFlow<EventoCalendario.Entrenamiento?> = _entrenamientoSeleccionado

    private val _partidoSeleccionado = MutableStateFlow<EventoCalendario.Partido?>(null)
    val partidoSeleccionado: StateFlow<EventoCalendario.Partido?> = _partidoSeleccionado

    private val _mesSeleccionado = MutableStateFlow(YearMonth.now())
    val mesSeleccionado: StateFlow<YearMonth> = _mesSeleccionado

    // Lista de partidos DTO para estadísticas
    private val _partidosParaEstadisticas = MutableStateFlow<List<EventoCalendario.Partido>>(emptyList())
    val partidosParaEstadisticas: StateFlow<List<EventoCalendario.Partido>> = _partidosParaEstadisticas

    private val _partidosState = MutableStateFlow<PartidoUIState>(PartidoUIState.Loading)
    val partidosState: StateFlow<PartidoUIState> = _partidosState

    fun seleccionarEntrenamiento(entrenamiento: EventoCalendario.Entrenamiento) {
        _entrenamientoSeleccionado.value = entrenamiento
    }

    fun seleccionarPartido(partido: EventoCalendario.Partido) {
        _partidoSeleccionado.value = partido
    }

    fun setMesSeleccionado(mes: YearMonth) {
        _mesSeleccionado.value = mes
    }

    fun getEventosForUser(idEquipo: Int) {
        _eventoState.value = EventoState.Loading

        viewModelScope.launch {
            try{
                println("Obteniendo eventos...")
                val eventos = idEquipo.let { CalendarioApi.getEventosPorEquipo(it) }

                if (eventos.isNotEmpty()){
                    _eventoState.value = EventoState.Success(eventos)
                } else {
                    _eventoState.value = EventoState.Error("No se encontraron eventos")
                }
            } catch (e: Exception) {
                _eventoState.value = EventoState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Carga los partidos de un equipo para luego seleccionar en la pantalla de estadísticas.
     */
    fun getPartidos(idEquipo: Int) {
        viewModelScope.launch {
            val partidos = CalendarioApi.getPartidos(idEquipo)
            _partidosParaEstadisticas.value = partidos
        }
    }

    fun resetState() {
        _eventoState.value = EventoState.Idle
    }

    fun obtenerEventoByMonth(equipoId: Int, mes: YearMonth) {
        _eventoState.value = EventoState.Loading
        viewModelScope.launch {
            try {
                val eventos = CalendarioRepository.getEventosForMonth(equipoId, mes)
                _eventoState.value = EventoState.Success(eventos)
            } catch (e: Exception) {
                _eventoState.value = EventoState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Actualización de eventos después de la creación o eliminación de un evento
    private fun actualizarEventos(idEquipo: Int, mes: YearMonth) {
        viewModelScope.launch {
            obtenerEventoByMonth(idEquipo, mes)
        }
    }

    /**
     * Llama a la API para crear un entrenamiento.
     * Tras crear el entrenamiento, se refrescan los eventos del equipo.
     */
    fun crearEntrenamiento(
        idEquipo: Int,
        fecha: LocalDate,
        descripcion: String?,
        entrenamientoUrl: String?,
        mes: YearMonth
    ) {
        _eventoState.value = EventoState.Loading
        viewModelScope.launch {
            try {
                println("Creando entrenamiento para el equipo $idEquipo...")
                val resultado: EntrenamientoDTO? =
                    CalendarioApi.crearEntrenamiento(idEquipo, fecha, descripcion, entrenamientoUrl)
                if (resultado != null) {
                    // Se ha creado el entrenamiento; se refrescan los eventos
                    println("Entrenamiento creado: $resultado")
                    CalendarioRepository.refrescarEventosEquipo(idEquipo)
                    actualizarEventos(idEquipo, mes)
                } else {
                    _eventoState.value = EventoState.Error("Error al crear entrenamiento")
                }
            } catch (e: Exception) {
                _eventoState.value = EventoState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Llama a la API para eliminar un entrenamiento.
     * Tras eliminar, se refrescan los eventos para ese equipo.
     */
    fun eliminarEntrenamiento(id: Int, idEquipo: Int, mes: YearMonth) {
        _eventoState.value = EventoState.Loading
        viewModelScope.launch {
            try {
                println("Eliminando entrenamiento con id $id...")
                val exito = CalendarioApi.eliminarEntrenamiento(id)
                if (exito) {
                    println("Entrenamiento eliminado")
                    CalendarioRepository.refrescarEventosEquipo(idEquipo)
                    actualizarEventos(idEquipo, mes)
                } else {
                    _eventoState.value = EventoState.Error("Error al eliminar entrenamiento")
                }
            } catch (e: Exception) {
                _eventoState.value = EventoState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Llama a la API para crear un partido.
     * Tras crear el partido, se refrescan los eventos del equipo.
     */
    fun crearPartido(idEquipo: Int, nombreRival: String, fecha: LocalDate, mes: YearMonth) {
        _eventoState.value = EventoState.Loading
        viewModelScope.launch {
            try {
                println("Creando partido para el equipo $idEquipo...")
                val resultado: PartidoDTO? =
                    CalendarioApi.crearPartido(idEquipo, nombreRival, fecha)
                if (resultado != null) {
                    println("Partido creado: $resultado")
                    CalendarioRepository.refrescarEventosEquipo(idEquipo)
                    actualizarEventos(idEquipo, mes)
                } else {
                    _eventoState.value = EventoState.Error("Error al crear el partido")
                }
            } catch (e: Exception) {
                _eventoState.value = EventoState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Llama a la API para actualizar un partido.
     * Tras actualizar, se refrescan los eventos del equipo.
     */
    fun actualizarPartido(
        partidoId: Int,
        resultadoNumerico: String,
        resultado: String,
        idEquipo: Int,
        mes: YearMonth,
        onResult: (Boolean, String?) -> Unit
    ) {
        _eventoState.value = EventoState.Loading
        viewModelScope.launch {
            try {
                println("Actualizando partido con id $partidoId...")
                val actualizado: PartidoDTO? = CalendarioApi.actualizarPartido(
                    partidoId,
                    resultadoNumerico,
                    resultado
                )
                if (actualizado != null) {
                    println("Partido actualizado: $actualizado")
                    //getEventosForUser(idEquipo)
                    CalendarioRepository.refrescarEventosEquipo(idEquipo)
                    actualizarEventos(idEquipo, mes)
                    onResult(true, null)
                } else {
                    val msg = "Error al actualizar el partido"
                    _eventoState.value = EventoState.Error(msg)
                    onResult(false, msg)
                }
            } catch (e: Exception) {
                val err = e.message ?: "Error desconocido"
                _eventoState.value = EventoState.Error(err)
                onResult(false, err)
            }
        }
    }

    fun actualizarJugadoresDestacados(
        partidoId: Int,
        jugadoresDestacados: String,
        idEquipo: Int,
        mes: YearMonth,
        onResult: (Boolean, String?) -> Unit
    ) {
        _eventoState.value = EventoState.Loading
        viewModelScope.launch {
            try {
                println("Actualizando partido con id $partidoId...")
                val actualizado: PartidoDTO? = CalendarioApi.actualizarJugadoresDestacados(
                    partidoId,
                    jugadoresDestacados
                )
                if (actualizado != null) {
                    println("Partido actualizado: $actualizado")
                    //getEventosForUser(idEquipo)
                    CalendarioRepository.refrescarEventosEquipo(idEquipo)
                    actualizarEventos(idEquipo, mes)
                    onResult(true, null)
                } else {
                    val msg = "Error al actualizar el partido"
                    _eventoState.value = EventoState.Error(msg)
                    onResult(false, msg)
                }
            } catch (e: Exception) {
                val err = e.message ?: "Error desconocido"
                _eventoState.value = EventoState.Error(err)
                onResult(false, err)
            }
        }
    }

    /**
     * Llama a la API para eliminar un partido.
     * Tras eliminar, se refrescan los eventos para ese equipo.
     */
    fun eliminarPartido(partidoId: Int, idEquipo: Int, mes: YearMonth) {
        _eventoState.value = EventoState.Loading
        viewModelScope.launch {
            try {
                println("Eliminando partido con id $partidoId...")
                val exito = CalendarioApi.eliminarPartido(partidoId)
                if (exito) {
                    println("Partido eliminado")
                    CalendarioRepository.refrescarEventosEquipo(idEquipo)
                    actualizarEventos(idEquipo, mes)
                } else {
                    _eventoState.value = EventoState.Error("Error al eliminar el partido")
                }
            } catch (e: Exception) {
                _eventoState.value = EventoState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun cargarPartidos(idEquipo: Int) {
        viewModelScope.launch {
            _partidosState.value = PartidoUIState.Loading
            try {
                val partidos = CalendarioApi.getPartidosByEquipo(idEquipo)
                _partidosState.value = PartidoUIState.Success(partidos)
            } catch (e: Exception) {
                _partidosState.value = PartidoUIState.Error("Error desconocido: ${e.message}")
            }
        }
    }

    fun limpiarCacheEventos() {
        CalendarioRepository.limpiarCache()
    }
}