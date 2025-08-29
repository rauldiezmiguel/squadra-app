package navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.kizitonwose.calendar.core.YearMonth
import com.kizitonwose.calendar.core.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import network.ActualizarJugadoresDestacadosRequest
import network.ActualizarResultadoPartidoRequest
import network.CrearEntrenamientoRequest
import network.CrearEstadisticasRequest
import network.CrearJugadorRequest
import network.CrearPartidoRequest
import network.EventoCalendario
import screens.ActualizarPartidoScreen
import screens.AnalisisEquipoPartidoScreen
import screens.AnalisisRivalPartidoScreen
import screens.AsistenciaEntrenamientoScreen
import screens.ChangePasswordScreen
import screens.CrearEntrenamientoScreen
import screens.CrearEstadisticasScreen
import screens.CrearEvaluacionScreen
import screens.CrearFichaJugadorScreen
import screens.CrearJugadorScreen
import screens.CrearPartidoScreen
import screens.EditarJugadorScreen
import screens.EquipoScreen
import screens.EvaluacionesJugadorScreen
import screens.FichaPartido
import screens.JugadorScreen
import screens.JugadoresEquipoScreen
import screens.LoginScreen
import screens.MainScreen
import screens.ModificarPartidoTabsScreen
import screens.VerAnalisisEquipoPartidoScreen
import screens.VerAnalisisRivalPartidoScreen
import screens.VerAsistenciaEntrenamientosJugadorScreen
import screens.VerDetalleEstadisticaEquipoScreen
import screens.VerDetalleEstadisticaScreen
import screens.VerEntrenamientoScreen
import screens.VerEstadisticasTotalesJugadorScreen
import screens.VerPartidoScreen
import screens.VerEstadisticasTotalesEquipoScreen
import screens.VerFichasJugadoresScreen
import screens.VerListaEntrenamientoScreen
import screens.VerListaPartidosScreen
import screens.VerPartidoTabsScreen
import screens.VerPerfilUsuarioScreen
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
import viewModel.ChangePasswordState
import viewModel.JugadorState


@Composable
fun AppNavigation(authViewModel: AndroidAuthViewModel, equipoViewModel: AndroidEquipoViewModel, calendarioViewModel: AndroidCalendarioViewModel, jugadorViewModel: AndroidJugadorViewModel, estadisticasViewModel: AndroidEstadisticasViewModel, asistenciaEntrenamientoViewModel: AndroidAsistenciaEntrenamientoViewModel, evaluacionViewModel: AndroidEvaluacionViewModel, usuarioViewModel: AndroidUsuarioViewModel, cuartosEquipoViewModel: AndroidCuartosEquipoViewModel, alineacionEquipoViewModel: AndroidAlineacionEquipoViewModel, cuartosRivalViewModel: AndroidCuartosRivalViewModel, alineacionRivalViewModel: AndroidAlineacionRivalViewModel, navController: NavHostController) {

    val authState by authViewModel.authState.collectAsState()

    // Definir las animaciones de transición entre pantallas
    NavHost(
        navController = navController,
        startDestination = Routes.Login.route
    ) {
        composable(
            Routes.Login.route,
            enterTransition = {
                // Animación de entrada: Slide desde la derecha
                slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn()
            },
            exitTransition = {
                // Animación de salida: Slide hacia la izquierda
                slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut()
            }
        ) {
            if (authState is AuthState.Authenticated) {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else {
                LoginScreen(authViewModel) {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }

        composable(
            Routes.Main.route,
            enterTransition = {
                // Animación de entrada: Fade in
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                // Animación de salida: Fade out
                fadeOut(animationSpec = tween(700))
            }
        ) {
            val onNavigateToMain: () -> Unit = {
                navController.navigate(
                    Routes.Main.route
                ) {
                    popUpTo(0) { inclusive = true } // Elimina toda la pila
                    launchSingleTop = true          // Evita múltiples instancias si ya está en top
                }
            }

            val onNavigateToVerEntrenamientosEquipo: () -> Unit = {
                navController.navigate(Routes.VerListaEntrenamiento.route) {
                    popUpTo(Routes.Main.route) { saveState = true }
                    launchSingleTop = true
                }
            }

            val onNavigateToVerPartidosEquipo: () -> Unit = {
                navController.navigate(Routes.VerListaPartidos.route) {
                    popUpTo(Routes.Main.route) { saveState = true }
                    launchSingleTop = true
                }
            }

            val onProfile: () -> Unit = {
                navController.navigate(Routes.VerPerfilUsuario.route) {
                    popUpTo(Routes.Main.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            // Si ya no estás autenticado (viniste de un logout), redirige a Login:
            if (authState is AuthState.Unauthenticated) {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } else {
                MainScreen(
                    equipoViewModel = equipoViewModel,
                    onTeamSelected = { idEquipo, nombreEquipo, idTemporada ->
                        val encodedNombre = Uri.encode(nombreEquipo)
                        navController.navigate(Routes.Equipo.createRoute(idEquipo, encodedNombre, idTemporada)) {
                            popUpTo(Routes.Main.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }},
                    navController = navController,
                    onProfile = onProfile,
                    authViewModel = authViewModel,
                    onNavigateToMain = onNavigateToMain,
                    onNavigateToVerEntrenamientosEquipo = onNavigateToVerEntrenamientosEquipo,
                    onNavigateToVerPartidosEquipo = onNavigateToVerPartidosEquipo
                )
            }
        }

        composable(
            Routes.Equipo.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("nombreEquipo") { type = NavType.StringType },
                navArgument("idTemporada") { type = NavType.IntType }
            ),
            enterTransition = {
                // Animación de entrada: Fade in
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                // Animación de salida: Fade out
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0
            val nombreEquipo = Uri.decode(backStackEntry.arguments?.getString("nombreEquipo") ?: "Equipo")
            val idTemporada = backStackEntry.arguments?.getInt("idTemporada") ?: 0
            val encodedNombre = Uri.encode(nombreEquipo)

            val onEstadisticaSelect: () -> Unit = {
                val encodeNombre = Uri.encode(nombreEquipo)
                navController.navigate(Routes.VerEstadisticasTotalesEquipo.createRoute(idEquipo, idTemporada, encodeNombre)) {
                    // 1) si ya existía esa ruta arriba en la pila, no la duplica:
                    launchSingleTop = true
                    // 2) opcional: limpiar cualquier pantalla que esté por encima de "Equipo"
                    popUpTo(Routes.Equipo.route) { saveState = true }
                    // 3) restaurar estado si usamos restoreState en navegación previa
                    restoreState = true
                }
            }

            val onJugadoresSelect: () -> Unit = {
                val encodeNombre = Uri.encode(nombreEquipo)
                navController.navigate(Routes.JugadoresEquipo.createRoute(idEquipo, encodeNombre)) {
                    launchSingleTop = true
                    popUpTo(Routes.Equipo.route) { saveState = true }
                    restoreState = true
                }
            }

            val onNavigateToMain: () -> Unit = {
                navController.navigate(
                    Routes.Main.route
                ) {
                    popUpTo(0) { inclusive = true } // Elimina toda la pila
                    launchSingleTop = true          // Evita múltiples instancias si ya está en top
                }
            }

            val onNavigateToVerEntrenamientosEquipo: () -> Unit = {
                navController.navigate(Routes.VerListaEntrenamiento.route) {
                    popUpTo(Routes.Equipo.route) { saveState = true }
                    launchSingleTop = true
                }
            }

            val onNavigateToVerPartidosEquipo: () -> Unit = {
                navController.navigate(Routes.VerListaPartidos.route) {
                    popUpTo(Routes.Equipo.route) { saveState = true }
                    launchSingleTop = true
                }
            }

            EquipoScreen(
                onProfile = { navController.navigate(Routes.VerPerfilUsuario.route) {
                    popUpTo(Routes.Equipo.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                } },
                idEquipo = idEquipo,
                idTemporada = idTemporada,
                nombreEquipo = nombreEquipo,
                onJugadoresSelect = onJugadoresSelect,
                onEstadisticaSelect = onEstadisticaSelect,
                navController = navController,
                calendarioViewModel = calendarioViewModel,
                authViewModel = authViewModel,
                onNavigateToMain = onNavigateToMain,
                onNavigateToVerPartidosEquipo = onNavigateToVerPartidosEquipo,
                onNavigateToVerEntrenamientosEquipo = onNavigateToVerEntrenamientosEquipo
            )
        }

        composable(
            Routes.CrearEntrenamiento.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("selectDate") { type = NavType.StringType },
                navArgument("idTemporada") { type = NavType.IntType }
            ),
            enterTransition = {
                // Animación de entrada: Fade in
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                // Animación de salida: Fade out
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            // Extraer el id del equipo (si no existe, se usa 0 o puedes lanzar un error)
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0
            val idTemporada = backStackEntry.arguments?.getInt("idTemporada") ?: 0
            val nombreEquipo = backStackEntry.arguments?.getString("nombreEquipo") ?: ""
            val selectDateStr = backStackEntry.arguments?.getString("selectDate")
            val selectDate = selectDateStr?.let { LocalDate.parse(it) } ?: LocalDate.now()

            //Obtener el mes actual.
            val month = selectDate.month
            val year = selectDate.year

            val selectMonth = YearMonth(year, month)

            // CALLBACK onDismiss: Este callback se ejecutará cuando se cancele la operación.
            val onDismiss: () -> Unit = {
                // Navega a la pantalla del equipo utilizando la ruta del equipo.
                navController.popBackStack()
            }

            // CALLBACK onCreate: Recibe un objeto CrearEntrenamientoRequest y define qué hacer al crearlo.
            val onCreate: (CrearEntrenamientoRequest) -> Unit = { request ->
                // Llama a la función para crear el entrenamiento. Se asume que esta función refrescará el estado.
                calendarioViewModel.crearEntrenamiento(
                    idEquipo = idEquipo,
                    fecha = request.fecha,           // Por ejemplo, "2025-04-11"
                    descripcion = request.descripcion,
                    entrenamientoUrl = request.entrenamientoUrl,
                    mes = selectMonth
                )
            }
            CrearEntrenamientoScreen(
                onDismiss = onDismiss,
                onCreate = onCreate,
                idEquipo = idEquipo,
                fechaSeleccionada = selectDate.toJavaLocalDate(),
            )
        }

        composable(
            Routes.VerEntrenamiento.route,
            enterTransition = {
                // Animación de entrada: Fade in
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                // Animación de salida: Fade out
                fadeOut(animationSpec = tween(700))
            }
        ) {
            val entrenamiento by calendarioViewModel.entrenamientoSeleccionado.collectAsState()

            if (entrenamiento != null) {
                VerEntrenamientoScreen(
                    entrenamiento = entrenamiento!!,
                    onBack = { navController.popBackStack() }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontró el entrenamiento.")
                }
            }
        }

        composable(
            Routes.CrearPartido.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("selectDate") { type = NavType.StringType },
                navArgument("idTemporada") { type = NavType.IntType }
            ),
            enterTransition = {
                // Animación de entrada: Fade in
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                // Animación de salida: Fade out
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            // Extraer el id del equipo (si no existe, se usa 0 o puedes lanzar un error)
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0
            val idTemporada = backStackEntry.arguments?.getInt("idTemporada") ?: 0
            val nombreEquipo = backStackEntry.arguments?.getString("nombreEquipo") ?: ""
            val selectDateStr = backStackEntry.arguments?.getString("selectDate")
            val selectDate = selectDateStr?.let { LocalDate.parse(it) } ?: LocalDate.now()

            //Obtener el mes actual.
            val month = selectDate.month
            val year = selectDate.year

            val selectMonth = YearMonth(year, month)

            // CALLBACK onDismiss: Este callback se ejecutará cuando se cancele la operación.
            val onDismiss: () -> Unit = {
                // Navega a la pantalla del equipo utilizando la ruta del equipo.
                 navController.popBackStack()
            }

            // CALLBACK onCreate: Recibe un objeto CrearEntrenamientoRequest y define qué hacer al crearlo.
            val onCreate: (CrearPartidoRequest) -> Unit = { request ->
                // Llama a la función para crear el entrenamiento. Se asume que esta función refrescará el estado.
                calendarioViewModel.crearPartido(
                    idEquipo = idEquipo,
                    nombreRival = request.nombreRival,
                    fecha = selectDate,
                    mes = selectMonth
                )
            }
            CrearPartidoScreen(
                onDismiss = onDismiss,
                onCreate = onCreate,
                idEquipo = idEquipo,
                fechaSeleccionada = selectDate.toJavaLocalDate()
            )
        }

        composable(
            Routes.VerPartido.route,
            enterTransition = {
                // Animación de entrada: Fade in
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                // Animación de salida: Fade out
                fadeOut(animationSpec = tween(700))
            }
        ) {
            val partido by calendarioViewModel.partidoSeleccionado.collectAsState()

            if (partido != null) {
                VerPartidoScreen(
                    partido = partido!!,
                    jugadorViewModel = jugadorViewModel,
                    estadisticasViewModel = estadisticasViewModel
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontró el partido.")
                }
            }
        }

        composable(
            Routes.ActualizarPartido.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("idPartido") { type = NavType.IntType },
                navArgument("idTemporada") { type = NavType.IntType },
                navArgument("selectDate") { type = NavType.StringType }
            ),
            enterTransition = {
                // Animación de entrada: Fade in
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                // Animación de salida: Fade out
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            // Extraer el id del equipo (si no existe, se usa 0 o puedes lanzar un error)
            val idPartido = backStackEntry.arguments?.getInt("idPartido") ?: 0
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0
            val idTemporada = backStackEntry.arguments?.getInt("idTemporada") ?: 0
            val nombreEquipo = backStackEntry.arguments?.getString("nombreEquipo") ?: ""

            val selectDateStr = backStackEntry.arguments?.getString("selectDate")
            val selectDate = selectDateStr?.let { LocalDate.parse(it) } ?: LocalDate.now()

            //Obtener el mes actual.
            val month = selectDate.month
            val year = selectDate.year

            val selectMonth = YearMonth(year, month)

            // CALLBACK onDismiss: Este callback se ejecutará cuando se cancele la operación.
            val onDismiss: () -> Unit = {
                // Navega a la pantalla del equipo utilizando la ruta del equipo.
            }

            val onUpdatePartido: (ActualizarResultadoPartidoRequest, (Boolean, String?) -> Unit) -> Unit = { request, onResult ->
                calendarioViewModel.actualizarPartido(
                    id = idPartido,
                    resultadoNumerico = request.resultadoNumerico,
                    resultado = request.resultado,
                    idEquipo = idEquipo,
                    mes = selectMonth,
                    onResult = onResult
                )
            }

            val onCreateEstadistica: (CrearEstadisticasRequest) -> Unit = { request->
                estadisticasViewModel.crearEstadisticas(
                    request = request
                )
            }

            ActualizarPartidoScreen(
                idEquipo = idEquipo,
                idPartido = idPartido,
                jugadorViewModel = jugadorViewModel,
                onDismiss = onDismiss,
                onUpdatePartido = onUpdatePartido,
                onCreateEstadistica = onCreateEstadistica
            )
        }

        composable(
            Routes.JugadoresEquipo.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("nombreEquipo") { type = NavType.StringType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0
            val nombreEquipo = backStackEntry.arguments?.getString("nombreEquipo") ?: ""
            val encodedNombre = Uri.encode(nombreEquipo)

            val onNavigateToMain: () -> Unit = {
                navController.navigate(
                    Routes.Main.route
                ) {
                    popUpTo(0) { inclusive = true } // Elimina toda la pila
                    launchSingleTop = true          // Evita múltiples instancias si ya está en top
                }
            }

            val onNavigateToVerEntrenamientosEquipo: () -> Unit = {
                navController.navigate(Routes.VerListaEntrenamiento.route) {
                    popUpTo(Routes.JugadoresEquipo.route) { saveState = true }
                    launchSingleTop = true
                }
            }

            val onNavigateToVerPartidosEquipo: () -> Unit = {
                navController.navigate(Routes.VerListaPartidos.route) {
                    popUpTo(Routes.JugadoresEquipo.route) { saveState = true }
                    launchSingleTop = true
                }
            }

            val onVerFichaJugadores: () -> Unit = {
                navController.navigate(
                    Routes.VerFichasJugadores.createRoute(
                        idEquipo = idEquipo,
                        nombreEquipo = encodedNombre
                    )
                ) {
                    popUpTo(Routes.Jugador.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            JugadoresEquipoScreen(
                onProfile = { navController.navigate(Routes.VerPerfilUsuario.route) {
                    popUpTo(Routes.JugadoresEquipo.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                } },
                idEquipo = idEquipo,
                nombreEquipo = nombreEquipo,
                onJugadorSelected = { jugador ->
                    println("Jugador seleccionado: ${jugador.id} con dorsal ${jugador.dorsal}")
                    jugadorViewModel.seleccionarJugador(jugador)
                    navController.navigate(Routes.Jugador.createRoute(idEquipo, encodedNombre)) {
                        popUpTo(Routes.JugadoresEquipo.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onCreateJugador = { equipoId ->
                    // Navega a una pantalla para crear jugador (si tienes una), o muestra un modal, etc.
                    navController.navigate(Routes.CrearJugador.createRoute(equipoId, encodedNombre)){
                        popUpTo(Routes.JugadoresEquipo.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                jugadorViewModel = jugadorViewModel,
                authViewModel = authViewModel,
                onNavigateToMain = onNavigateToMain,
                onNavigateToVerEntrenamientosEquipo = onNavigateToVerEntrenamientosEquipo,
                onNavigateToVerPartidosEquipo = onNavigateToVerPartidosEquipo,
                onVerFichaJugadores = onVerFichaJugadores
            )

        }

        composable(
            Routes.CrearJugador.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("nombreEquipo") { type = NavType.StringType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0
            val nombreEquipo = backStackEntry.arguments?.getString("nombreEquipo") ?: ""

            // CALLBACK onDismiss: Este callback se ejecutará cuando se cancele la operación.
            val onDismiss: () -> Unit = {
                navController.popBackStack()
            }

            // CALLBACK onCreate: Recibe un objeto CrearEntrenamientoRequest y define qué hacer al crearlo.
            val onCreate: (CrearJugadorRequest) -> Unit = { request ->
                // Llama a la función para crear el entrenamiento. Se asume que esta función refrescará el estado.
                jugadorViewModel.crearJugador(request)
            }

            CrearJugadorScreen(
                idEquipo = idEquipo,
                onDismiss = onDismiss,
                onCreate = onCreate
            )
        }

        composable(
            Routes.Jugador.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("nombreEquipo") { type = NavType.StringType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0
            val nombreEquipo = backStackEntry.arguments?.getString("nombreEquipo") ?: ""

            val jugador = jugadorViewModel.jugadorSeleccionado

            val encodedNombre = Uri.encode(nombreEquipo)

            val onDismiss: () -> Unit = {
                // Navega a la pantalla del equipo utilizando la ruta del equipo.
                navController.popBackStack()
            }

            val onProfile: () -> Unit = {
                navController.navigate(Routes.VerPerfilUsuario.route) {
                    popUpTo(Routes.Jugador.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            val onVerEstadisticas: (Int) -> Unit = {
                jugador.let {
                    navController.navigate(
                        Routes.VerEstadisticasTotalesJugador.createRoute(
                            idJugador = it.value!!.id,
                            idTemporada = it.value!!.idTemporada,
                            nombreEquipo = encodedNombre
                        )
                    ) {
                        popUpTo(Routes.Jugador.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            val onAnadirEstadisticas: (Int) -> Unit = {
                jugador.let {
                    navController.navigate(
                        Routes.CrearEstadisticas.createRoute(
                            idEquipo = idEquipo,
                            idJugador = it.value!!.id
                        )
                    ) {
                        popUpTo(Routes.Jugador.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            val onVerEvaluaciones: (Int) -> Unit = {
                jugador.let {
                    navController.navigate(
                        Routes.VerEvaluacionesJugador.createRoute(
                            idJugador = it.value!!.id,
                            nombreEquipo = nombreEquipo
                        )
                    ) {
                        popUpTo(Routes.Jugador.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            val onAnadirEvaluaciones: (Int) -> Unit = {
                jugador.let {
                    navController.navigate(
                        Routes.CrearEvaluacion.createRoute(
                            idJugador = it.value!!.id,
                            idEquipo = idEquipo
                        )
                    ) {
                        popUpTo(Routes.Jugador.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            val onVerAsistenciaEntrenamientos: (Int) -> Unit = {
                jugador.let {
                    navController.navigate(
                        Routes.VerAsistenciaEntrenamientoJugador.createRoute(
                            idJugador = it.value!!.id,
                            nombreEquipo = nombreEquipo
                        )
                    ) {
                        popUpTo(Routes.Jugador.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            val onNavigateToMain: () -> Unit = {
                navController.navigate(
                    Routes.Main.route
                ) {
                    popUpTo(0) { inclusive = true } // Elimina toda la pila
                    launchSingleTop = true          // Evita múltiples instancias si ya está en top
                }
            }

            val onNavigateToVerEntrenamientosEquipo: () -> Unit = {
                navController.navigate(Routes.VerListaEntrenamiento.route) {
                    popUpTo(Routes.Jugador.route) { saveState = true }
                    launchSingleTop = true
                }
            }

            val onNavigateToVerPartidosEquipo: () -> Unit = {
                navController.navigate(Routes.VerListaPartidos.route) {
                    popUpTo(Routes.Jugador.route) { saveState = true }
                    launchSingleTop = true
                }
            }

            val onCrearFichaJugador: (Int) -> Unit = {
                jugador.let {
                    navController.navigate(
                        Routes.CrearFichaJugador.createRoute(
                            idJugador = it.value!!.id,
                            idEquipo = idEquipo
                        )
                    ) {
                        popUpTo(Routes.Jugador.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            val  onEditarJugador: (Int) -> Unit = { idJugador ->
                navController.navigate(Routes.EditarJugador.createRoute(idJugador = idJugador)){
                    popUpTo(Routes.Jugador.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            JugadorScreen(
                idEquipo = idEquipo,
                nombreEquipo = nombreEquipo,
                navController = navController,
                jugadorViewModel = jugadorViewModel,
                authViewModel = authViewModel,
                onProfile = onProfile,
                onVerEstadisticas = onVerEstadisticas,
                onAnadirEstadisticas = onAnadirEstadisticas,
                onVerEvaluaciones = onVerEvaluaciones,
                onAnadirEvaluaciones = onAnadirEvaluaciones,
                onVerAsistenciaEntrenamientos = onVerAsistenciaEntrenamientos,
                onNavigateToMain = onNavigateToMain,
                onNavigateToVerEntrenamientosEquipo = onNavigateToVerEntrenamientosEquipo,
                onNavigateToVerPartidosEquipo = onNavigateToVerPartidosEquipo,
                onCrearFichaJugador = onCrearFichaJugador,
                onEditarJugador = onEditarJugador
            )
        }

        composable(
            Routes.CrearEstadisticas.route,
            arguments = listOf(
                navArgument("idJugador")   { type = NavType.IntType },
                navArgument("idEquipo")    { type = NavType.IntType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idJugador   = backStackEntry.arguments!!.getInt("idJugador")
            val idEquipo    = backStackEntry.arguments!!.getInt("idEquipo")

            CrearEstadisticasScreen(
                idJugador = idJugador,
                idEquipo = idEquipo,
                calendarioViewModel = calendarioViewModel,
                onDismiss = { navController.popBackStack() },
                onCreate = { request ->
                    estadisticasViewModel.crearEstadisticas(request)
                    navController.popBackStack()
                }
            )
        }

        composable(
            Routes.VerEstadisticasTotalesJugador.route,
            arguments = listOf(
                navArgument("idJugador") { type = NavType.IntType },
                navArgument("idTemporada") { type = NavType.IntType },
                navArgument("nombreEquipo") { type = NavType.StringType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idJugador   = backStackEntry.arguments!!.getInt("idJugador")
            val idTemporada = backStackEntry.arguments!!.getInt("idTemporada")
            val nombreEquipo = backStackEntry.arguments?.getString("nombreEquipo") ?: ""

            VerEstadisticasTotalesJugadorScreen(
                idJugador = idJugador,
                idTemporada = idTemporada,
                nombreEquipo = nombreEquipo,
                estadisticasViewModel = estadisticasViewModel,
                jugadorViewModel = jugadorViewModel,
                onNavigateToDetalle = { nombreEstadistica, idEquipo ->
                    println("Has viajado a la estadistica: $nombreEstadistica")
                    navController.navigate(Routes.VerDetalleEstadistica.createRoute(idEquipo = idEquipo, idTemporada = idTemporada, nomEstadistica = nombreEstadistica, nombreEquipo = nombreEquipo)) {
                        popUpTo(Routes.VerEstadisticasTotalesJugador.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.VerDetalleEstadistica.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("idTemporada") { type = NavType.IntType },
                navArgument("nomEstadistica") { type = NavType.StringType },
                navArgument("nombreEquipo") { type = NavType.StringType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ){ backStackEntry ->
            val idEquipo = backStackEntry.arguments!!.getInt("idEquipo")
            val idTemporada = backStackEntry.arguments!!.getInt("idTemporada")
            val nomEstadistica = backStackEntry.arguments?.getString("nomEstadistica") ?: ""
            val nombreEquipo = backStackEntry.arguments?.getString("nombreEquipo") ?: ""

            VerDetalleEstadisticaScreen(
                idEquipo = idEquipo,
                idTemporada = idTemporada,
                nomEstadistica = nomEstadistica,
                nombreEquipo = nombreEquipo,
                estadisticasViewModel = estadisticasViewModel,
                jugadorViewModel = jugadorViewModel
            )
        }

        composable(
            Routes.AsistenciaEntrenamiento.route,
            arguments = listOf(
                navArgument("idEntrenamiento") { type = NavType.IntType },
                navArgument("nombreEquipo")    { type = NavType.StringType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idEnt = backStackEntry.arguments!!.getInt("idEntrenamiento")
            val nombre = Uri.decode(backStackEntry.arguments!!.getString("nombreEquipo")!!)
            AsistenciaEntrenamientoScreen(
                idEntrenamiento = idEnt,
                nombreEquipo = nombre,
                asistenciaEntrenamientoViewModel = asistenciaEntrenamientoViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.VerAsistenciaEntrenamientoJugador.route,
            arguments = listOf(
                navArgument("idJugador") { type = NavType.IntType },
                navArgument("nombreEquipo")    { type = NavType.StringType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idJugador = backStackEntry.arguments?.getInt("idJugador") ?: return@composable
            val nombre = Uri.decode(backStackEntry.arguments!!.getString("nombreEquipo")!!)

            VerAsistenciaEntrenamientosJugadorScreen(
                idJugador = idJugador,
                nombreEquipo = nombre,
                asistenciaEntrenamientoViewModel = asistenciaEntrenamientoViewModel,
                jugadorViewModel = jugadorViewModel
            )
        }

        composable(
            Routes.CrearEvaluacion.route,
            arguments = listOf(
                navArgument("idJugador")   { type = NavType.IntType },
                navArgument("idEquipo")    { type = NavType.IntType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idJugador = backStackEntry.arguments?.getInt("idJugador")
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo")

            val onCreated: () -> Unit = {
                // Navega a la pantalla del equipo utilizando la ruta del equipo.
                navController.popBackStack()
            }

            if (idJugador != null) {
                CrearEvaluacionScreen(
                    idJugador = idJugador,
                    viewModel = evaluacionViewModel,
                    onCreated = onCreated
                )
            }
        }

        composable(
            Routes.VerEvaluacionesJugador.route,
            arguments = listOf(
                navArgument("idJugador")   { type = NavType.IntType },
                navArgument("nombreEquipo")    { type = NavType.StringType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idJugador = backStackEntry.arguments?.getInt("idJugador")
            val nombre = Uri.decode(backStackEntry.arguments!!.getString("nombreEquipo")!!)

            if (idJugador != null) {
                EvaluacionesJugadorScreen(
                    idJugador = idJugador,
                    nombreEquipo = nombre,
                    viewModel =  evaluacionViewModel,
                    jugadorViewModel = jugadorViewModel
                )
            }
        }

        composable(
            Routes.VerEstadisticasTotalesEquipo.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("idTemporada") { type = NavType.IntType },
                navArgument("nombreEquipo") { type = NavType.StringType }
            ),
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) }
        ) { backStackEntry ->
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0
            val idTemporada = backStackEntry.arguments?.getInt("idTemporada") ?: 0
            val nombreEquipo = Uri.decode(backStackEntry.arguments?.getString("nombreEquipo") ?: "")

            VerEstadisticasTotalesEquipoScreen(
                idEquipo = idEquipo,
                idTemporada = idTemporada,
                nombreEquipo = nombreEquipo,
                estadisticasViewModel = estadisticasViewModel,
                onNavigateToDetalle = { nomEstadistica ->
                    val encodedNombre = Uri.encode(nombreEquipo)
                    navController.navigate(
                        Routes.VerDetalleEstadisticaEquipo.createRoute(idEquipo, idTemporada, nomEstadistica, encodedNombre)
                    ) {
                        popUpTo(Routes.VerEstadisticasTotalesEquipo.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.VerDetalleEstadisticaEquipo.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("idTemporada") { type = NavType.IntType },
                navArgument("nomEstadistica") { type = NavType.StringType },
                navArgument("nombreEquipo") { type = NavType.StringType }
            ),
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) }
        ) { backStackEntry ->
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0
            val idTemporada = backStackEntry.arguments?.getInt("idTemporada") ?: 0
            val nomEstadistica = backStackEntry.arguments?.getString("nomEstadistica") ?: ""
            val nombreEquipo = Uri.decode(backStackEntry.arguments?.getString("nombreEquipo") ?: "")

            VerDetalleEstadisticaEquipoScreen(
                idEquipo = idEquipo,
                idTemporada = idTemporada,
                nomEstadistica = nomEstadistica,
                nombreEquipo = nombreEquipo,
                viewModel = estadisticasViewModel
            )
        }

        composable(
            Routes.VerPerfilUsuario.route,
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) }
        ) {
            val onLogout: () -> Unit = {
                authViewModel.logout()
                navController.navigate(Routes.Login.route) {
                    // Borra todo el back‑stack hasta el inicio real del grafo
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }

            VerPerfilUsuarioScreen(
                viewModel = usuarioViewModel,
                onBack = { navController.popBackStack() },
                onLogout = onLogout,
                onChangePassword = {
                    navController.navigate("changePassword")
                }
            )
        }

        composable(
            Routes.VerListaPartidos.route,
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) }
        ) {
            val onPartidoSelect: (EventoCalendario) -> Unit = { partido ->
                navController.navigate(Routes.VerPartidoTabs.createRoute(idEquipo = partido.idEquipo)) {
                    launchSingleTop = true
                    popUpTo(Routes.VerListaPartidos.route) { saveState = true }
                    restoreState = true
                }
            }

            VerListaPartidosScreen (
                viewModel = calendarioViewModel,
                equipoViewModel = equipoViewModel,
                onPartidoSelect = onPartidoSelect
            )
        }

        composable(
            Routes.CrearFichaJugador.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("idJugador") { type = NavType.IntType }
            ),
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) }
        ) { backStackEntry ->
            val idJugador = backStackEntry.arguments?.getInt("idJugador") ?: return@composable
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: return@composable

            CrearFichaJugadorScreen(
                idJugador = idJugador,
                idEquipo = idEquipo,
                viewModel = jugadorViewModel,
                onCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            Routes.VerFichasJugadores.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("nombreEquipo") { type = NavType.StringType }
            ),
            enterTransition = {
                // Animación de entrada: Fade in
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                // Animación de salida: Fade out
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: return@composable
            val nombreEquipo = backStackEntry.arguments?.getString("nombreEquipo") ?: return@composable

            VerFichasJugadoresScreen(
                idEquipo = idEquipo,
                nombreEquipo = nombreEquipo,
                viewModel = jugadorViewModel
            )
        }

        composable(
            Routes.VerListaEntrenamiento.route,
            enterTransition = {
                // Animación de entrada: Fade in
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                // Animación de salida: Fade out
                fadeOut(animationSpec = tween(700))
            }
        ) {

            val onEntrenamientoSelect: () -> Unit = {
                navController.navigate(Routes.VerEntrenamiento.route) {
                    launchSingleTop = true
                    popUpTo(Routes.VerListaEntrenamiento.route) { saveState = true }
                    restoreState = true
                }
            }

            VerListaEntrenamientoScreen(
                viewModel = calendarioViewModel,
                equipoViewModel = equipoViewModel,
                onEntrenamientoSelect = onEntrenamientoSelect
            )
        }

        composable(
            Routes.AnalisisEquipoPartido.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("nombreEquipo") { type = NavType.StringType },
                navArgument("idPartido") { type = NavType.IntType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0
            val nombreEquipo = backStackEntry.arguments?.getString("nombreEquipo") ?: ""
            val encodedNombre = Uri.encode(nombreEquipo)
            val idPartido = backStackEntry.arguments?.getInt("idPartido") ?: 0

            // CALLBACK onCreate: Recibe un objeto CrearEntrenamientoRequest y define qué hacer al crearlo.
            val onGuardar: (FichaPartido) -> Unit = { request ->
                // Llama a la función para crear el entrenamiento. Se asume que esta función refrescará el estado.
                navController.popBackStack()
            }

            AnalisisEquipoPartidoScreen(
                onGuardar = onGuardar,
                idEquipo = idEquipo,
                idPartido = idPartido,
                jugadorViewModel = jugadorViewModel,
                cuartosEquipoViewModel = cuartosEquipoViewModel,
                alineacionEquipoViewModel = alineacionEquipoViewModel
            )
        }

        composable(
            Routes.AnalisisRivalPartido.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("nombreEquipo") { type = NavType.StringType },
                navArgument("idPartido") { type = NavType.IntType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0
            val nombreEquipo = backStackEntry.arguments?.getString("nombreEquipo") ?: ""
            val encodedNombre = Uri.encode(nombreEquipo)
            val idPartido = backStackEntry.arguments?.getInt("idPartido") ?: 0

            // CALLBACK onCreate: Recibe un objeto CrearEntrenamientoRequest y define qué hacer al crearlo.
            val onGuardar: () -> Unit = {
                // Llama a la función para crear el entrenamiento. Se asume que esta función refrescará el estado.
                navController.popBackStack()
            }

            val onDismiss: () -> Unit = {
                // Navega a la pantalla del equipo utilizando la ruta del equipo.
                navController.popBackStack()
            }
        }

        composable(
            Routes.ModificarPartidoTabs.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("idPartido") { type = NavType.IntType },
                navArgument("idTemporada") { type = NavType.IntType },
                navArgument("selectDate") { type = NavType.StringType },
                navArgument("nombreEquipo") { type = NavType.StringType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            // Extraer el id del equipo (si no existe, se usa 0 o puedes lanzar un error)
            val idPartido = backStackEntry.arguments?.getInt("idPartido") ?: 0
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0
            val idTemporada = backStackEntry.arguments?.getInt("idTemporada") ?: 0
            val nombreEquipo = backStackEntry.arguments?.getString("nombreEquipo") ?: ""
            val encodedNombre = Uri.encode(nombreEquipo)

            val selectDateStr = backStackEntry.arguments?.getString("selectDate")
            val selectDate = selectDateStr?.let { LocalDate.parse(it) } ?: LocalDate.now()

            //Obtener el mes actual.
            val month = selectDate.month
            val year = selectDate.year

            val selectMonth = YearMonth(year, month)

            // CALLBACK onDismiss: Este callback se ejecutará cuando se cancele la operación.
            val onDismiss: () -> Unit = {
                // Navega a la pantalla del equipo utilizando la ruta del equipo.
                navController.popBackStack()
            }

            // CALLBACK onCreate: Recibe un objeto CrearEntrenamientoRequest y define qué hacer al crearlo.
            val onUpdatePartido: (ActualizarResultadoPartidoRequest, (Boolean, String?) -> Unit) -> Unit = { request, onResult ->
                calendarioViewModel.actualizarPartido(
                    id = idPartido,
                    resultadoNumerico = request.resultadoNumerico,
                    resultado = request.resultado,
                    idEquipo = idEquipo,
                    mes = selectMonth,
                    onResult = onResult
                )
            }

            val onUpdateJugadoresDestacados: (ActualizarJugadoresDestacadosRequest, (Boolean, String?) -> Unit) -> Unit = { request, onResult ->
                calendarioViewModel.actualizarJugadoresDestacados(
                    partidoId = idPartido,
                    jugadoresDestacados = request.jugadoresDestacados,
                    idEquipo = idEquipo,
                    mes = selectMonth,
                    onResult = onResult
                )
            }

            val onCreateEstadistica: (CrearEstadisticasRequest) -> Unit = { request->
                estadisticasViewModel.crearEstadisticas(
                    request = request
                )
            }

            // CALLBACK onCreate: Recibe un objeto CrearEntrenamientoRequest y define qué hacer al crearlo.
            val onGuardar: (FichaPartido) -> Unit = { request ->
                // Llama a la función para crear el entrenamiento. Se asume que esta función refrescará el estado.
                navController.popBackStack()
            }

            val onLogout: () -> Unit = {
                authViewModel.logout()
                navController.navigate(Routes.Login.route) {
                    // Borra todo el back‑stack hasta el inicio real del grafo
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }

            val onGuardarRival: () -> Unit = {
                // Llama a la función para crear el entrenamiento. Se asume que esta función refrescará el estado.
                navController.popBackStack()
            }

            ModificarPartidoTabsScreen(
                onDismiss = onDismiss,
                onCreateEstadistica = onCreateEstadistica,
                onUpdatePartido = onUpdatePartido,
                onUpdateJugadoresDestacados = onUpdateJugadoresDestacados,
                idEquipo = idEquipo,
                idPartido = idPartido,
                onGuardar = onGuardar,
                onGuardarRival = onGuardarRival,
                jugadorViewModel = jugadorViewModel,
                viewModel = usuarioViewModel,
                cuartosEquipoViewModel = cuartosEquipoViewModel,
                alineacionEquipoViewModel = alineacionEquipoViewModel,
                cuartosRivalViewModel = cuartosRivalViewModel,
                alineacionRivalViewModel = alineacionRivalViewModel,
                onBack = { navController.popBackStack() },
                onLogout = onLogout
            )
        }

        composable(
            Routes.VerAnalisisEquipoPartido.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("idPartido") { type = NavType.IntType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0
            val idPartido = backStackEntry.arguments?.getInt("idPartido") ?: 0

            VerAnalisisEquipoPartidoScreen(
                idEquipo = idEquipo,
                idPartido = idPartido,
                jugadorViewModel = jugadorViewModel,
                cuartosEquipoViewModel = cuartosEquipoViewModel,
                alineacionEquipoViewModel = alineacionEquipoViewModel
            )
        }

        composable(
            Routes.VerAnalisisRivalPartido.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType },
                navArgument("idPartido") { type = NavType.IntType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0
            val idPartido = backStackEntry.arguments?.getInt("idPartido") ?: 0
            val partido by calendarioViewModel.partidoSeleccionado.collectAsState()
            
            VerAnalisisRivalPartidoScreen(
                idEquipo = idEquipo,
                idPartido = idPartido,
                cuartosRivalViewModel = cuartosRivalViewModel,
                alineacionRivalViewModel = alineacionRivalViewModel,
                partido = partido!!
            )
        }

        composable(
            Routes.VerPartidoTabs.route,
            arguments = listOf(
                navArgument("idEquipo") { type = NavType.IntType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idEquipo = backStackEntry.arguments?.getInt("idEquipo") ?: 0

            val partido by calendarioViewModel.partidoSeleccionado.collectAsState()

            VerPartidoTabsScreen(
                idEquipo = idEquipo,
                idPartido = partido!!.idPartido,
                partido = partido!!,
                jugadorViewModel = jugadorViewModel,
                cuartosEquipoViewModel = cuartosEquipoViewModel,
                alineacionEquipoViewModel = alineacionEquipoViewModel,
                cuartosRivalViewModel = cuartosRivalViewModel,
                alineacionRivalViewModel = alineacionRivalViewModel,
                estadisticasViewModel = estadisticasViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.EditarJugador.route,
            arguments = listOf(
                navArgument("idJugador") { type = NavType.IntType },
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(700))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val idJugador = backStackEntry.arguments?.getInt("idJugador") ?: 0

            // CALLBACK onBack: Este callback se ejecutará cuando se cancele la operación.
            val onBack: () -> Unit = {
                // Navega a la pantalla del equipo utilizando la ruta del equipo.
                navController.popBackStack()
            }

            // CALLBACK onCreate: Recibe un objeto CrearEntrenamientoRequest y define qué hacer al crearlo.
            val onGuardar: (Int, String) -> Unit = { dorsal, nombre ->
                jugadorViewModel.actualizarJugador(idJugador, dorsal, nombre)
            }

            EditarJugadorScreen(
                idJugador = idJugador,
                jugadorViewModel = jugadorViewModel,
                onBack = onBack,
                onGuardar = onGuardar
            )
        }

        composable(
            Routes.ChangePassword.route,
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) }
        ) {
            val changePasswordState by usuarioViewModel.changePassword.collectAsState()

            ChangePasswordScreen(
                onBack = { navController.popBackStack() },
                onSubmit = { current, new ->
                    // Aquí llamas a tu ViewModel para hacer la request al backend
                    usuarioViewModel.changePassword(current, new)
                },
                usuarioViewModel = usuarioViewModel
            )
        }
    }
}