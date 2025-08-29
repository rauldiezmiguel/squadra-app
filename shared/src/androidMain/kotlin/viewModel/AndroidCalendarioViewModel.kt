package viewModel

import androidx.lifecycle.ViewModel
import com.kizitonwose.calendar.core.YearMonth
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import network.EntrenamientoDTO
import network.EventoCalendario
import network.ResultadoPartido
import storage.TokenStorage

class AndroidCalendarioViewModel(tokenStorage: TokenStorage) : ViewModel() {
    private val calendarioViewModel = CalendarioViewModel(tokenStorage)

    val eventoState: StateFlow<EventoState> = calendarioViewModel.eventoState

    val entrenamientoSeleccionado: StateFlow<EventoCalendario.Entrenamiento?> = calendarioViewModel.entrenamientoSeleccionado

    val partidoSeleccionado: StateFlow<EventoCalendario.Partido?> = calendarioViewModel.partidoSeleccionado

    val mesSeleccionado: StateFlow<YearMonth> = calendarioViewModel.mesSeleccionado

    val partidosParaEstadisticas: StateFlow<List<EventoCalendario.Partido>> = calendarioViewModel.partidosParaEstadisticas

    val partidosState: StateFlow<PartidoUIState> = calendarioViewModel.partidosState

    fun seleccionarEntrenamiento(entrenamiento: EventoCalendario.Entrenamiento) {
        calendarioViewModel.seleccionarEntrenamiento(entrenamiento)
    }

    fun seleccionarPartido(partido: EventoCalendario.Partido) {
        calendarioViewModel.seleccionarPartido(partido)
    }

    fun setMesSeleccionado(mes: YearMonth) {
        calendarioViewModel.setMesSeleccionado(mes)
    }

    fun getEventosForUser(idEquipo: Int) {
        calendarioViewModel.getEventosForUser(idEquipo)
    }

    fun getPartidos(idEquipo: Int) {
        calendarioViewModel.getPartidos(idEquipo)
    }

    fun resetState() {
        calendarioViewModel.resetState()
    }

    fun obtenerEventosByMonth(equipoId: Int, month: YearMonth){
        calendarioViewModel.obtenerEventoByMonth(equipoId, month)
    }

    fun crearEntrenamiento(
        idEquipo: Int,
        fecha: LocalDate,
        descripcion: String?,
        entrenamientoUrl: String?,
        mes: YearMonth
    ){
        calendarioViewModel.crearEntrenamiento(idEquipo, fecha, descripcion, entrenamientoUrl, mes)
    }

    fun eliminarEntrenamiento(
        id: Int,
        idEquipo: Int,
        mes: YearMonth
    ){
        calendarioViewModel.eliminarEntrenamiento(id, idEquipo, mes)
    }

    fun crearPartido(
        idEquipo: Int,
        nombreRival: String,
        fecha: LocalDate,
        mes: YearMonth
    ){
        calendarioViewModel.crearPartido(idEquipo, nombreRival, fecha, mes)
    }

    fun actualizarPartido(
        id: Int,
        resultadoNumerico: String,
        resultado: String,
        idEquipo: Int,
        mes: YearMonth,
        onResult: (Boolean, String?) -> Unit
    ){
        calendarioViewModel.actualizarPartido(id, resultadoNumerico, resultado, idEquipo, mes, onResult)
    }

    fun actualizarJugadoresDestacados(
        partidoId: Int,
        jugadoresDestacados: String,
        idEquipo: Int,
        mes: YearMonth,
        onResult: (Boolean, String?) -> Unit
    ) {
        calendarioViewModel.actualizarJugadoresDestacados(partidoId, jugadoresDestacados, idEquipo, mes, onResult)
    }

    fun eliminarPartido(
        id: Int,
        idEquipo: Int,
        mes: YearMonth
    ){
        calendarioViewModel.eliminarPartido(id, idEquipo, mes)
    }

    fun cargarPartidos(idEquipo: Int) {
        calendarioViewModel.cargarPartidos(idEquipo)
    }
}