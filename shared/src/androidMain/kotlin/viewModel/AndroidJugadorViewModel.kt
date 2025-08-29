package viewModel

import androidx.lifecycle.ViewModel
import com.kizitonwose.calendar.core.YearMonth
import kotlinx.coroutines.flow.StateFlow
import network.CrearJugadorRequest
import network.EventoCalendario
import network.FichaJugadorRequest
import network.JugadorDTO
import storage.TokenStorage

class AndroidJugadorViewModel(tokenStorage: TokenStorage): ViewModel() {
    private val commonJugadorViewModel = JugadorViewModel(tokenStorage)

    val jugadorState get() = commonJugadorViewModel.jugadorState

    val jugadorSeleccionado: StateFlow<JugadorDTO?> = commonJugadorViewModel.jugadorSeleccionado
    val crearFichaJugadorState: StateFlow<CrearFichaJugadorState> = commonJugadorViewModel.crearFichaJugadorState
    val fichasJugadoresState: StateFlow<FichasJugadoresState> = commonJugadorViewModel.fichasJugadoresState
    val fichaJugadorState: StateFlow<FichaJugadorState> = commonJugadorViewModel.fichaJugadorState
    val lastUpdated: StateFlow<String?> = commonJugadorViewModel.lastUpdated

    fun seleccionarJugador(jugador: JugadorDTO) {
        commonJugadorViewModel.seleccionarJugador(jugador)
    }

    fun getJugadoresPorEquipo(idEquipo: Int) {
        commonJugadorViewModel.getJugadoresPorEquipo(idEquipo)
    }

    fun getJugadoresPorId(idJugador: Int) {
        commonJugadorViewModel.getJugadoresPorId(idJugador)
    }

    fun crearJugador(request: CrearJugadorRequest) {
        commonJugadorViewModel.crearJugador(request)
    }

    fun eliminarJugador(idJugador: Int, idEquipo: Int) {
        commonJugadorViewModel.eliminarJugador(idJugador, idEquipo)
    }

    fun actualizarJugador(idJugador: Int, dorsal: Int, posicion: String) {
        commonJugadorViewModel.actualizarJugador(idJugador, dorsal, posicion)
    }

    fun crearFichaJugador(request: FichaJugadorRequest) {
        commonJugadorViewModel.crearFichaJugador(request)
    }

    fun eliminarFichaJugador(idFichaJugador: Int) {
        commonJugadorViewModel.eliminarFichaJugador(idFichaJugador)
    }

    fun obtenerFichaDeJugador(idJugador: Int) {
        commonJugadorViewModel.obtenerFichaDeJugador(idJugador)
    }

    fun obtenerFichaDeJugadorPorEquipo(idEquipo: Int) {
        commonJugadorViewModel.obtenerFichaDeJugadorPorEquipo(idEquipo)
    }

    fun getLastUpdatedPorEquipo(idEquipo: Int) {
        commonJugadorViewModel.getLastUpdatedPorEquipo(idEquipo)
    }
}