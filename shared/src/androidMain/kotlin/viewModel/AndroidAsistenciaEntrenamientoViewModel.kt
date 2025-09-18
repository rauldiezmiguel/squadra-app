package viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import network.AsistenciaEntrenamientoDTO
import storage.TokenStorage

class AndroidAsistenciaEntrenamientoViewModel(tokenStorage: TokenStorage): ViewModel() {
    private val commonAsistenciaEntrenamientoViewModel = AsistenciaEntrenamientoViewModel(tokenStorage)

    val asistenciasState: StateFlow<AsistenciaEntrenamientoState> = commonAsistenciaEntrenamientoViewModel.asistenciasState
    val asistenciaJugadorState: StateFlow<AsistenciaEntrenamientoJugadorState> = commonAsistenciaEntrenamientoViewModel.asistenciasJugadorState
    val guardadoState: StateFlow<GuardadoState> = commonAsistenciaEntrenamientoViewModel.guardadoState

    fun loadAsistencias(entrenamientoId: Int) {
        commonAsistenciaEntrenamientoViewModel.loadAsistencias(entrenamientoId)
    }

    fun saveAsistencias(onComplete: () -> Unit = {}) {
        commonAsistenciaEntrenamientoViewModel.saveAsistencias(onComplete)
    }
    fun toggleAsistencia(idJugador: Int, asistio: Boolean) {
        commonAsistenciaEntrenamientoViewModel.toggleAsistencia(idJugador, asistio)
    }

    fun getAsistenciasPorJugador(idJugador: Int) {
        commonAsistenciaEntrenamientoViewModel.getAsistenciasPorJugador(idJugador)
    }

    fun actualizarMotivo(idJugador: Int, motivo: String) {
        commonAsistenciaEntrenamientoViewModel.actualizarMotivo(idJugador, motivo)
    }

    fun obtenerMotivo(idJugador: Int): String {
        return commonAsistenciaEntrenamientoViewModel.obtenerMotivo(idJugador)
    }
}