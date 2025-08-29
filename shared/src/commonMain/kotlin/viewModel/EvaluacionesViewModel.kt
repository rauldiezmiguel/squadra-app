package viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.CrearEvaluacionRequest
import network.EvaluacionApi
import network.EvaluacionDTO
import network.PromedioMensualDTO

sealed class EvaluacionState {
    data object Idle : EvaluacionState()
    data object Loading : EvaluacionState()
    data class Success(val evaluaciones: List<EvaluacionDTO>) : EvaluacionState()
    data class Error(val message: String) : EvaluacionState()
}

sealed class PromedioMensualState {
    data object Idle : PromedioMensualState()
    data object Loading : PromedioMensualState()
    data class Success(val promedios: List<PromedioMensualDTO>) : PromedioMensualState()
    data class Error(val message: String) : PromedioMensualState()
}

class EvaluacionViewModel : CommonViewModel() {
    private val _evaluacionState = MutableStateFlow<EvaluacionState>(EvaluacionState.Idle)
    val evaluacionState: StateFlow<EvaluacionState> = _evaluacionState

    private val _promediosMensualesState = MutableStateFlow<PromedioMensualState>(PromedioMensualState.Idle)
    val promediosMensualesState: StateFlow<PromedioMensualState> = _promediosMensualesState

    fun getEvaluaciones(idJugador: Int) {
        _evaluacionState.value = EvaluacionState.Loading

        viewModelScope.launch {
            val evaluaciones = EvaluacionApi.getEvaluaciones(idJugador)
            if (evaluaciones != null) {
                _evaluacionState.value = EvaluacionState.Success(evaluaciones)
            } else {
                _evaluacionState.value = EvaluacionState.Error("No se pudieron cargar las evaluaciones")
            }
        }
    }

    fun getPromediosMensuales(idJugador: Int) {
        _promediosMensualesState.value = PromedioMensualState.Loading

        viewModelScope.launch {
            val promedios = EvaluacionApi.getPromediosMensuales(idJugador)
            if (promedios != null) {
                _promediosMensualesState.value = PromedioMensualState.Success(promedios)
            } else {
                _promediosMensualesState.value = PromedioMensualState.Error("No se pudieron cargar los promedios")
            }
        }
    }

    fun crearEvaluacion(idJugador: Int, request: CrearEvaluacionRequest) {
        _evaluacionState.value = EvaluacionState.Loading

        viewModelScope.launch {
            val nueva = EvaluacionApi.crearEvaluacion(request)
            if (nueva != null) {
                getEvaluaciones(idJugador)
            } else {
                _evaluacionState.value = EvaluacionState.Error("No se pudo crear la evaluación")
            }
        }
    }

    fun eliminarEvaluacion(idEvaluacion: Int, idJugador: Int) {
        _evaluacionState.value = EvaluacionState.Loading

        viewModelScope.launch {
            val ok = EvaluacionApi.eliminarEvaluacion(idEvaluacion)
            if (ok) {
                getEvaluaciones(idJugador)
            } else {
                _evaluacionState.value = EvaluacionState.Error("No se pudo eliminar la evaluación")
            }
        }
    }
}
