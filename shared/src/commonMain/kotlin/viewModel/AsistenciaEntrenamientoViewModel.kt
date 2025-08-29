package viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.AsistenciaEntrenamientoDTO
import network.AsistenciaEntrenamientosApi
import storage.TokenStorage

sealed class AsistenciaEntrenamientoState {
    data object Loading : AsistenciaEntrenamientoState()
    data class Success(val asistencias: List<AsistenciaEntrenamientoDTO>) : AsistenciaEntrenamientoState()
    data class Error(val message: String) : AsistenciaEntrenamientoState()
}

sealed class AsistenciaEntrenamientoJugadorState {
    data object Loading : AsistenciaEntrenamientoJugadorState()
    data class Success(val asistencias: List<AsistenciaEntrenamientoDTO>) : AsistenciaEntrenamientoJugadorState()
    data class Error(val message: String) : AsistenciaEntrenamientoJugadorState()
}

class AsistenciaEntrenamientoViewModel(private val tokenStorage: TokenStorage) : CommonViewModel() {
    private val _asistenciasState = MutableStateFlow<AsistenciaEntrenamientoState>(AsistenciaEntrenamientoState.Loading)
    val asistenciasState: StateFlow<AsistenciaEntrenamientoState> = _asistenciasState

    private val _asistenciasJugadorState = MutableStateFlow<AsistenciaEntrenamientoJugadorState>(AsistenciaEntrenamientoJugadorState.Loading)
    val asistenciasJugadorState: StateFlow<AsistenciaEntrenamientoJugadorState> = _asistenciasJugadorState

    private val cambiosPendientes = mutableMapOf<Int, Boolean>()

    private val motivosInasistencia = mutableMapOf<Int, String>()

    /**
     * Lanza la petición para obtener las asistencias de un entrenamiento
     * y actualiza el estado en consecuencia.
     */
    fun loadAsistencias(idEntrenamiento: Int) {
        viewModelScope.launch {
            _asistenciasState.value = AsistenciaEntrenamientoState.Loading
            try {
                val listaOriginal = AsistenciaEntrenamientosApi.getAsistencias(idEntrenamiento)

                val listaConValoresIniciales = if (listaOriginal.all { it.asistio == false }) {
                    listaOriginal.map { it.copy(asistio = true) }
                } else {
                    listaOriginal
                }

                _asistenciasState.value = AsistenciaEntrenamientoState.Success(listaConValoresIniciales)
            } catch (e: Exception) {
                _asistenciasState.value = AsistenciaEntrenamientoState.Error(
                    e.message ?: "Error desconocido al cargar asistencias"
                )
            }
        }
    }

    /**
     * Guarda las asistencias modificadas (puedes llamar a esto cuando el usuario
     * pulse el botón "Guardar").
     */
    fun saveAsistencias(onComplete: () -> Unit = {}) {
        val current = _asistenciasState.value
        if (current is AsistenciaEntrenamientoState.Success) {
            viewModelScope.launch {
                try {
                    // 1) Construye la lista actualizada con los cambios
                    val listaActualizada = current.asistencias.map { dto ->
                        dto.copy(
                            asistio = cambiosPendientes[dto.idJugador] ?: dto.asistio,
                            motivoInasistencia = motivosInasistencia[dto.idJugador]
                            )
                    }

                    // 2) Enviar al backend
                    AsistenciaEntrenamientosApi.saveAsistencias(listaActualizada)

                    // 3) Limpiar cambios temporales
                    cambiosPendientes.clear()
                    motivosInasistencia.clear()

                    // 4) Recargar del backend
                    val idEnt = listaActualizada.firstOrNull()?.idEntrenamiento
                    if (idEnt != null) {
                        val recargada = AsistenciaEntrenamientosApi.getAsistencias(idEnt)
                        _asistenciasState.value = AsistenciaEntrenamientoState.Success(recargada)
                    }

                    // 5) Callback final
                    onComplete()
                } catch (e: Exception) {
                    _asistenciasState.value = AsistenciaEntrenamientoState.Error(
                        e.message ?: "Error al guardar asistencias"
                    )
                }
            }
        }
    }

    /**
     * Permite alternar localmente el flag de asistencia de un jugador
     * (usado por el checkbox en pantalla).
     */
    fun toggleAsistencia(idJugador: Int, asistio: Boolean) {
        cambiosPendientes[idJugador] = asistio

        val current = _asistenciasState.value
        if (current is AsistenciaEntrenamientoState.Success) {
            // Actualiza solo para la UI (mezclando cambios + original)
            val nuevaLista = current.asistencias.map { dto ->
                if (dto.idJugador == idJugador) dto.copy(asistio = asistio)
                else dto
            }
            _asistenciasState.value = AsistenciaEntrenamientoState.Success(nuevaLista)
        }
    }

    fun getAsistenciasPorJugador(idJugador: Int) {
        viewModelScope.launch {
            _asistenciasJugadorState.value = AsistenciaEntrenamientoJugadorState.Loading
            try {
                val lista = AsistenciaEntrenamientosApi.getAsistenciasPorJugador(idJugador)
                _asistenciasJugadorState.value = AsistenciaEntrenamientoJugadorState.Success(lista)
            } catch (e: Exception) {
                _asistenciasJugadorState.value = AsistenciaEntrenamientoJugadorState.Error(
                    e.message ?: "Error desconocido al cargar asistencias del jugador"
                )
            }
        }
    }

    fun actualizarMotivo(idJugador: Int, motivo: String) {
        motivosInasistencia[idJugador] = motivo
    }

    fun obtenerMotivo(idJugador: Int): String {
        return motivosInasistencia[idJugador] ?: ""
    }
}