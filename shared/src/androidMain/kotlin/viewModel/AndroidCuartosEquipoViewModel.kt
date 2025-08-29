package viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import network.CuartosEquipoDTO
import network.CuartosEquipoPartidoDTO
import network.ModificarCuartosEquipoRequest
import network.ModificarCuartosRivalRequest

class AndroidCuartosEquipoViewModel : ViewModel() {
    private val commonCuartosEquipoViewModel = CuartosEquipoViewModel()

    val cuartoEquipoState: StateFlow<CuartoEquipoState> = commonCuartosEquipoViewModel.cuartoEquipoState
    val cuartosEquipo: StateFlow<List<CuartosEquipoPartidoDTO>> = commonCuartosEquipoViewModel.cuartosEquipo
    val cuartoActulizadoEquipoState: StateFlow<CuartoActualizadoEquipoState> = commonCuartosEquipoViewModel.cuartoActulizadoEquipoState

    fun obtenerCuartosEquipoPorPartido(idPartido: Int) {
        commonCuartosEquipoViewModel.obtenerCuartosEquipoPorPartido(idPartido)
    }

    fun actualizarCuartoEquipo(id: Int, request: ModificarCuartosEquipoRequest) {
        commonCuartosEquipoViewModel.actualizarCuartoEquipo(id, request)
    }
}