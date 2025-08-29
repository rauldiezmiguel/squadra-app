package viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import network.EquipoApi

import network.EquipoDTO
import repository.EquipoRepository
import storage.TokenStorage

sealed class EquipoState {
    data object Idle : EquipoState()
    data object Loading : EquipoState()
    data class Success(val equipos: List<EquipoDTO>) : EquipoState()
    data class Error(val message: String) : EquipoState()
}

sealed class CategoriaState {
    data object Loading: CategoriaState()
    data class Success(val categoria: String) : CategoriaState()
    data class Error(val message: String) : CategoriaState()
}

class EquipoViewModel(private val tokenStorage: TokenStorage) : CommonViewModel() {
    private val _equipoState = MutableStateFlow<EquipoState>(EquipoState.Loading)
    val equipoState: StateFlow<EquipoState> = _equipoState

    private val _categoriaState = MutableStateFlow<CategoriaState>(CategoriaState.Loading)
    val categoriaState: StateFlow<CategoriaState> = _categoriaState

    fun getEquiposForUser() {

        // Si los equipos ya están en memoria, los mostramos sin llamar a la API
        EquipoRepository.getEquipos()?.let {
            _equipoState.value = EquipoState.Success(it)
            return
        }

        _equipoState.value = EquipoState.Loading

        viewModelScope.launch {
            try {
                println("Obteniendo equipos...")
                val equipos = EquipoApi.getEquiposForUser()
                println("Equipos obtenidos: $equipos")

                if (!equipos.isNullOrEmpty()) {
                    val ordenados = equipos.ordenadosPorCategoria()
                    EquipoRepository.saveEquipos(ordenados) // Guardamos en memoria
                    _equipoState.value = EquipoState.Success(ordenados)
                } else {
                    println("No se encontraron equipos.")
                    _equipoState.value = EquipoState.Error("No se encontraron equipos")
                }
            } catch (e: Exception) {
                _equipoState.value = EquipoState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getCategoriaById(id: Int) {
        _categoriaState.value = CategoriaState.Loading

        viewModelScope.launch {
            try {
                println("Obteniendo categoria...")
                val categoria = EquipoApi.getCategoriaById(id)
                println("Categoria obtenida: $categoria")

                if (!categoria.isNullOrEmpty()) {
                    _categoriaState.value = CategoriaState.Success(categoria)
                } else {
                    _categoriaState.value = CategoriaState.Error("Error a la hora de obtención de la categoría")
                }
            } catch (e: Exception) {
                _categoriaState.value = CategoriaState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Por ejemplo, cuando se cree, actualice o elimine un equipo, se debe llamar a:
    fun refreshEquipos() {
        EquipoRepository.clear() // Marca el caché como sucio
        getEquiposForUser()
    }

    fun clear() {
        EquipoRepository.clear()
    }

    private fun List<EquipoDTO>.ordenadosPorCategoria(): List<EquipoDTO> {
        val categoriasOrden = listOf(
            "Sub 7", "Sub 8", "Sub 9", "Sub 10", "Sub 11", "Sub 12",
            "Sub 13", "Sub 14", "Sub 15", "Cadete", "Juvenil"
        )

        return this.sortedWith(compareBy(
            { equipo ->
                // Extrae la categoría base (por ejemplo: "Sub 8" o "Cadete")
                categoriasOrden.indexOfFirst { categoria ->
                    equipo.nombreEquipo.startsWith(categoria, ignoreCase = true)
                }.let { if (it == -1) Int.MAX_VALUE else it }
            },
            { equipo ->
                // Extrae la letra del final (A, B, C...)
                val match = Regex("""([A-Z])$""").find(equipo.nombreEquipo)
                match?.value ?: "Z" // Si no hay letra, lo pone al final
            }
        ))
    }
}