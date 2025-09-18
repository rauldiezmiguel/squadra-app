package viewModel

import kotlinx.coroutines.flow.StateFlow
import network.ClubDTO

class AndroidClubesViewModel: CommonViewModel() {
    private val commonClubViewModel = ClubesViewModel()

    val allClubes: StateFlow<AllClubesState> = commonClubViewModel.allClubes
    val createClub: StateFlow<CreateClubState> = commonClubViewModel.createClub
    val actualizarClub: StateFlow<ActualizarClubState> = commonClubViewModel.actualizarClub
    val eliminarClub: StateFlow<EliminarClubState> = commonClubViewModel.eliminarClub

    fun getAllClubes() {
        commonClubViewModel.getAllClubes()
    }

    fun createClub(nombreClub: String, direccion: String?, telefono: String?, localizacion: String?) {
        commonClubViewModel.createClub(nombreClub, direccion, telefono, localizacion)
    }

    fun actualizarClub(id: Int, nombreClub: String, direccion: String?, telefono: String?, localizacion: String?) {
        commonClubViewModel.actualizarClub(id, nombreClub, direccion, telefono, localizacion)
    }

    fun eliminarClub(id: Int) {
        commonClubViewModel.eliminarClub(id)
    }
}