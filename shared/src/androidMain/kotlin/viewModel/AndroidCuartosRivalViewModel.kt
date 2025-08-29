package viewModel

import kotlinx.coroutines.flow.StateFlow
import network.CuartosRivalPartidoDTO
import network.ModificarCuartosRivalRequest

class AndroidCuartosRivalViewModel {
    private val commonCuartosRivalViewModel = CuartosRivalViewModel()

    val cuartoRivalState: StateFlow<CuartoRivalState> = commonCuartosRivalViewModel.cuartoRivalState
    val cuartosRival: StateFlow<List<CuartosRivalPartidoDTO>> = commonCuartosRivalViewModel.cuartosRival
    val cuartoActualizadoRivalState: StateFlow<CuartoActualizadoRivalState> = commonCuartosRivalViewModel.cuartoActualizadoRivalState

    fun obtenerCuartosRivalPorPartido(idPartido: Int) {
        commonCuartosRivalViewModel.obtenerCuartosRivalPorPartido(idPartido)
    }

    fun actualizarCuartoRival(id: Int, request: ModificarCuartosRivalRequest) {
        commonCuartosRivalViewModel.actualizarCuartoRival(id, request)
    }


}