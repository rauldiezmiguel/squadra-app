package viewModel

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import network.AlineacionEquipoDTO
import network.AlineacionEquipoUI
import network.CrearAlineacionEquipoRequest
import network.JugadorDTO

class AndroidAlineacionEquipoViewModel {
    private val commonAlineacionEquipoViewModel = AlineacionEquipoViewModel()

    val alineacionEquipoState: StateFlow<AlineacionEquipoState> = commonAlineacionEquipoViewModel.alineacionEquipoState
    val alineaciones: StateFlow<List<AlineacionEquipoDTO>> = commonAlineacionEquipoViewModel.alineaciones
    val addPlayer: StateFlow<AddPlayerState> = commonAlineacionEquipoViewModel.addPlayer
    val alineacionesPorCuarto = commonAlineacionEquipoViewModel.alineacionesPorCuarto

    val alineacionesUI: StateFlow<List<AlineacionEquipoUI>> = commonAlineacionEquipoViewModel.alineacionesUI

    fun actualizarJugadores(nuevaLista: List<JugadorDTO>) {
        commonAlineacionEquipoViewModel.actualizarJugadores(nuevaLista)
    }

    fun updatePlayerAlineacion(id: Int, posX: Float, posY: Float) {
        commonAlineacionEquipoViewModel.updatePlayerAlineacion(id, posX, posY)
    }

    fun obtenerAlineaciones(idCuarto: Int) {
        commonAlineacionEquipoViewModel.obtenerAlineaciones(idCuarto)
    }

    fun crearAlineacion(request: CrearAlineacionEquipoRequest) {
        commonAlineacionEquipoViewModel.crearAlineacion(request)
    }

    fun eliminarAlineacion(id: Int) {
        commonAlineacionEquipoViewModel.eliminarAlineacion(id)
    }
}