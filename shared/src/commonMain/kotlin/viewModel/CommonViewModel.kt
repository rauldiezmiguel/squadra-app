package viewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Clase base para ViewModels en el módulo común. Maneja un CoroutineScope
 * para lanzar corrutinas sin depender del ciclo de vida de Android.
 */
open class CommonViewModel {
    // Se utiliza un SupervisorJob para que las corrutinas puedan fallar individualmente
    private val viewModelJob = SupervisorJob()

    // Se define un scope principal; en common normalmente se usa Dispatchers.Main,
    // pero si se requiere otro se puede parametrizar
    protected val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Llamar a esta función para cancelar las corrutinas cuando el ViewModel ya no se necesite.
     */
    open fun stop() {
        viewModelScope.cancel()
    }
}