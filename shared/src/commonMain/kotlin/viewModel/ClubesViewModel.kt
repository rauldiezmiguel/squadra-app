package viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.ClubDTO
import network.ClubesApi

sealed class AllClubesState {
    data object Loading : AllClubesState()
    data class Success(val temporadas: List<ClubDTO>) : AllClubesState()
    data class Error(val message: String) : AllClubesState()
}

sealed class CreateClubState {
    data object Loading : CreateClubState()
    data class Success(val create: Boolean) : CreateClubState()
    data class Error(val message: String) : CreateClubState()
}

sealed class ActualizarClubState {
    data object Loading : ActualizarClubState()
    data class Success(val update: Boolean) : ActualizarClubState()
    data class Error(val message: String) : ActualizarClubState()
}

sealed class EliminarClubState {
    data object Loading : EliminarClubState()
    data class Success(val eliminate: Boolean) : EliminarClubState()
    data class Error(val message: String) : EliminarClubState()
}

class ClubesViewModel : CommonViewModel() {
    private val _allClubes = MutableStateFlow<AllClubesState>(AllClubesState.Loading)
    val allClubes: StateFlow<AllClubesState> = _allClubes

    private val _createClub = MutableStateFlow<CreateClubState>(CreateClubState.Loading)
    val createClub: StateFlow<CreateClubState> = _createClub

    private val _actualizarClub = MutableStateFlow<ActualizarClubState>(ActualizarClubState.Loading)
    val actualizarClub: StateFlow<ActualizarClubState> = _actualizarClub

    private val _eliminarClub = MutableStateFlow<EliminarClubState>(EliminarClubState.Loading)
    val eliminarClub: StateFlow<EliminarClubState> = _eliminarClub

    fun getAllClubes() {
        viewModelScope.launch {
            _allClubes.value = AllClubesState.Loading
            try {
                val response = ClubesApi.getAllClubes()
                if (response.isNotEmpty()) {
                    _allClubes.value = AllClubesState.Success(response)
                } else {
                    _allClubes.value = AllClubesState.Error("Error al obtener los clubes.")
                }
            } catch (e: Exception) {
                _allClubes.value = AllClubesState.Error("Error desconocido: ${e.message}")
            }
        }
    }

    fun createClub(nombreClub: String, direccion: String?, telefono: String?, localizacion: String?) {
        viewModelScope.launch {
            _createClub.value = CreateClubState.Loading
            try {
                val response = ClubesApi.createClub(nombreClub, direccion, telefono, localizacion)
                if (response) {
                    _createClub.value = CreateClubState.Success(response)
                } else {
                    _createClub.value = CreateClubState.Error("Error creando el nuevo club.")
                }
            } catch (e: Exception) {
                _createClub.value = CreateClubState.Error("Error desconocido: ${e.message}")
            }
        }
    }

    fun actualizarClub(id: Int, nombreClub: String, direccion: String?, telefono: String?, localizacion: String?) {
        viewModelScope.launch {
            _actualizarClub.value = ActualizarClubState.Loading
            try {
                val response = ClubesApi.actualizarClub(id, nombreClub, direccion, telefono, localizacion)
                if (response) {
                    _actualizarClub.value = ActualizarClubState.Success(response)
                } else {
                    _actualizarClub.value = ActualizarClubState.Error("Error actualizando el club con ID $id")
                }
            } catch (e: Exception) {
                _actualizarClub.value = ActualizarClubState.Error("Error desconocido: ${e.message}")
            }
        }
    }

    fun eliminarClub(id: Int) {
        viewModelScope.launch {
            _eliminarClub.value = EliminarClubState.Loading
            try {
                val response = ClubesApi.eliminarClub(id)
                if (response) {
                    _eliminarClub.value = EliminarClubState.Success(response)
                } else {
                    _eliminarClub.value = EliminarClubState.Error("Error eliminando el club con ID $id")
                }
            } catch (e: Exception) {
                _eliminarClub.value = EliminarClubState.Error("Error desconocido: ${e.message}")
            }
        }
    }
}