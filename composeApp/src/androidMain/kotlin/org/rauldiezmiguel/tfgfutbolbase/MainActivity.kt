package org.rauldiezmiguel.tfgfutbolbase

import viewModel.AuthViewModel
import navigation.AppNavigation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import navigation.Routes
import network.setupAuthApi
import storage.AndroidTokenStorageProvider
import viewModel.AndroidAlineacionEquipoViewModel
import viewModel.AndroidAlineacionRivalViewModel
import viewModel.AndroidAsistenciaEntrenamientoViewModel
import viewModel.AndroidAuthViewModel
import viewModel.AndroidCalendarioViewModel
import viewModel.AndroidCuartosEquipoViewModel
import viewModel.AndroidCuartosRivalViewModel
import viewModel.AndroidEquipoViewModel
import viewModel.AndroidEstadisticasViewModel
import viewModel.AndroidEvaluacionViewModel
import viewModel.AndroidJugadorViewModel
import viewModel.AndroidUsuarioViewModel
import viewModel.AuthState
import viewModel.EquipoViewModel

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AndroidAuthViewModel
    private lateinit var equipoViewModel: AndroidEquipoViewModel
    private lateinit var calendarioViewModel: AndroidCalendarioViewModel
    private lateinit var jugadorViewModel: AndroidJugadorViewModel
    private lateinit var estadisticasViewModel: AndroidEstadisticasViewModel
    private lateinit var asistenciaEntrenamientoViewModel: AndroidAsistenciaEntrenamientoViewModel
    private lateinit var evaluacionViewModel: AndroidEvaluacionViewModel
    private lateinit var usuarioViewModel: AndroidUsuarioViewModel
    private lateinit var cuartosEquipoViewModel: AndroidCuartosEquipoViewModel
    private lateinit var alineacionEquipoViewModel: AndroidAlineacionEquipoViewModel
    private lateinit var cuartosRivalViewModel: AndroidCuartosRivalViewModel
    private lateinit var alineacionRivalViewModel: AndroidAlineacionRivalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenStorageProvider = AndroidTokenStorageProvider(this)
        val tokenStorage = tokenStorageProvider.getTokenStorage()

        setupAuthApi(tokenStorage)

        authViewModel = AndroidAuthViewModel(tokenStorage)
        equipoViewModel = AndroidEquipoViewModel(tokenStorage)
        calendarioViewModel = AndroidCalendarioViewModel(tokenStorage)
        jugadorViewModel = AndroidJugadorViewModel(tokenStorage)
        estadisticasViewModel = AndroidEstadisticasViewModel()
        asistenciaEntrenamientoViewModel = AndroidAsistenciaEntrenamientoViewModel(tokenStorage)
        evaluacionViewModel = AndroidEvaluacionViewModel()
        usuarioViewModel = AndroidUsuarioViewModel()
        cuartosEquipoViewModel = AndroidCuartosEquipoViewModel()
        alineacionEquipoViewModel = AndroidAlineacionEquipoViewModel()
        cuartosRivalViewModel = AndroidCuartosRivalViewModel()
        alineacionRivalViewModel = AndroidAlineacionRivalViewModel()

        setContent {
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                authViewModel.checkExistingSession()
            }

            AppNavigation(
                navController    = navController,
                authViewModel    = authViewModel,
                equipoViewModel  = equipoViewModel,
                calendarioViewModel = calendarioViewModel,
                jugadorViewModel = jugadorViewModel,
                estadisticasViewModel = estadisticasViewModel,
                asistenciaEntrenamientoViewModel = asistenciaEntrenamientoViewModel,
                evaluacionViewModel = evaluacionViewModel,
                usuarioViewModel = usuarioViewModel,
                cuartosEquipoViewModel = cuartosEquipoViewModel,
                alineacionEquipoViewModel = alineacionEquipoViewModel,
                cuartosRivalViewModel = cuartosRivalViewModel,
                alineacionRivalViewModel = alineacionRivalViewModel
            )
        }
    }
}