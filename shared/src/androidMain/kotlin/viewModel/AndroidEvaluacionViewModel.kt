package viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import network.CrearEvaluacionRequest
import storage.TokenStorage

class AndroidEvaluacionViewModel(): ViewModel() {
    private val commonEvaluacionViewModel = EvaluacionViewModel()

    val evaluacionState: StateFlow<EvaluacionState> = commonEvaluacionViewModel.evaluacionState
    val promediosMensualesState: StateFlow<PromedioMensualState> = commonEvaluacionViewModel.promediosMensualesState

    fun getEvaluaciones(idJugador: Int) {
        commonEvaluacionViewModel.getEvaluaciones(idJugador)
    }

    fun getPromediosMensuales(idJugador: Int) {
        commonEvaluacionViewModel.getPromediosMensuales(idJugador)
    }

    fun crearEvaluacion(idJugador: Int, request: CrearEvaluacionRequest) {
        commonEvaluacionViewModel.crearEvaluacion(idJugador, request)
    }

    fun eliminarEvaluacion(idEvaluacion: Int, idJugador: Int) {
        commonEvaluacionViewModel.eliminarEvaluacion(idEvaluacion, idJugador)
    }
}