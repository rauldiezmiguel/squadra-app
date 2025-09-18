package viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class AndroidTemporadasViewModel(): ViewModel() {
    private val commonTemporadasViewModel = TemporadasViewModel()

    val allTemporadas: StateFlow<AllTemporadasState> = commonTemporadasViewModel.allTemporadas

    fun getAllTemporadas() {
        commonTemporadasViewModel.getAllTemporadas()
    }
}