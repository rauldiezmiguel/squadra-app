package viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import network.CrearEstadisticasRequest
import network.JugadorDTO

class AndroidEstadisticasViewModel : ViewModel() {
    private val commonEstadisticasViewModel = EstadisticasViewModel()
    val estadisticasTotalJugadorState: StateFlow<EstadisticaTotalJugadorState> = commonEstadisticasViewModel.estadisticasTotalJugadorState
    val detalleState: StateFlow<DetallePartidoState> = commonEstadisticasViewModel.detalleState
    val jugadorDetalleState: StateFlow<EstadisticaJugadorDetalleState> = commonEstadisticasViewModel.jugadorDetalleState
    val teamDetalleState: StateFlow<TeamDetalleState> = commonEstadisticasViewModel.teamDetalleState
    val estadisticasTotalesEquipoState: StateFlow<EstadisticasTotalesEquipoState> = commonEstadisticasViewModel.estadisticasTotalesEquipoState
    val estadisticasJugadorPartido: StateFlow<EstadisticasJugadorPartidoState> = commonEstadisticasViewModel.estadisticasJugadorPartido

    fun crearEstadisticas(request: CrearEstadisticasRequest) {
        commonEstadisticasViewModel.crearEstadisticas(request)
    }

    fun getEstadisticasTotalesJugador(idJugador: Int, idTemporada: Int) {
        commonEstadisticasViewModel.getEstadisticasTotalesJugador(idJugador, idTemporada)
    }

    fun cargarEstadisticaPartidos(idEquipo: Int, idTemporada: Int, nomEstadistica: String) {
        commonEstadisticasViewModel.cargarEstadisticaPartidos(idEquipo, idTemporada, nomEstadistica)
    }

    fun getEstadisticasByJugadorByPartido(idJugador: Int, idPartido: Int) {
        commonEstadisticasViewModel.getEstadisticasByJugadorByPartido(idJugador, idPartido)
    }

    fun getEstadisticasDeJugadoresParaPartido(jugadores: List<JugadorDTO>, idPartido: Int) {
        commonEstadisticasViewModel.getEstadisticasDeJugadoresParaPartido(jugadores, idPartido)
    }

    fun cargarEstadisticaJugadorPartidos(idJugador: Int, idEquipo: Int, idTemporada: Int, nomEstadistica: String) {
        commonEstadisticasViewModel.cargarEstadisticaJugadorPartidos(idJugador, idEquipo, idTemporada, nomEstadistica)
    }

    fun cargarDetalleEstadisticaEquipo(idEquipo: Int, idTemporada: Int, nomEstadistica: String) {
        commonEstadisticasViewModel.cargarDetalleEstadisticaEquipo(idEquipo, idTemporada, nomEstadistica)
    }

    fun getEstadisticasTotalesEquipo(idEquipo: Int, idTemporada: Int) {
        commonEstadisticasViewModel.getEstadisticasTotalesEquipo(idEquipo, idTemporada)
    }
}