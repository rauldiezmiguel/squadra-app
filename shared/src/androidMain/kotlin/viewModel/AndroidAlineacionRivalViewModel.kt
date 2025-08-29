package viewModel

import kotlinx.coroutines.flow.StateFlow
import network.AlineacionRivalDTO
import network.CrearAlineacionRivalRequest

class AndroidAlineacionRivalViewModel {
    private val commonAlineacionRivalViewModel = AlineacionRivalViewModel()

    val alineacionRivalState: StateFlow<AlineacionRivalState> = commonAlineacionRivalViewModel.alineacionRivalState
    val alineaciones: StateFlow<List<AlineacionRivalDTO>> = commonAlineacionRivalViewModel.alineaciones
    val addPlayer: StateFlow<AddPlayerRivalState> = commonAlineacionRivalViewModel.addPlayer
    val alineacionesPorCuarto = commonAlineacionRivalViewModel.alineacionesPorCuarto

    fun obtenerAlineaciones(idCuarto: Int) {
        commonAlineacionRivalViewModel.obtenerAlineaciones(idCuarto)
    }

    fun crearAlineacion(request: CrearAlineacionRivalRequest) {
        commonAlineacionRivalViewModel.crearAlineacion(request)
    }

    fun eliminarAlineacion(id: Int) {
        commonAlineacionRivalViewModel.eliminarAlineacion(id)
    }

    fun updatePlayerRival(id: Int, posX: Float, posY: Float) {
        commonAlineacionRivalViewModel.updatePlayerRival(id, posX, posY)
    }
}