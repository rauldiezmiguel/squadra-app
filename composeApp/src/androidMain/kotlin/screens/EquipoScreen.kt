package screens

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.savedstate.savedState
import navigation.Routes
import viewModel.AndroidCalendarioViewModel
import viewModel.EventoState
import visual.CalendarioEquipo
import com.kizitonwose.calendar.core.YearMonth
import kotlinx.datetime.toKotlinLocalDate
import network.EventoCalendario
import java.time.LocalDate
import firebaseStorage.eliminarImagenDeFirebase
import viewModel.AndroidAuthViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Person

@Composable
fun EquipoScreen(
    onProfile: () -> Unit,
    idEquipo: Int,
    idTemporada: Int,
    nombreEquipo: String,
    onEstadisticaSelect: () -> Unit,
    onJugadoresSelect: () -> Unit,
    navController: NavController,
    calendarioViewModel: AndroidCalendarioViewModel,
    authViewModel: AndroidAuthViewModel,
    onNavigateToMain: () -> Unit,
    onNavigateToVerEntrenamientosEquipo: () -> Unit,
    onNavigateToVerPartidosEquipo: () -> Unit
){
    var menuExpanded by remember { mutableStateOf(false) }
    val currentMonth by calendarioViewModel.mesSeleccionado.collectAsState()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showEventDialog by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFF263238)
    val secondaryColor = Color(0xFF455A64)
    val backgroundColor = Color(0xFFF9FAFB)

    LaunchedEffect(currentMonth) {
        calendarioViewModel.obtenerEventosByMonth(idEquipo, currentMonth)
    }

    // Observa el estado del viewModel
    val eventoState by calendarioViewModel.eventoState.collectAsState()

    // Extrae los eventos si hay éxito
    val eventosMes = when (eventoState) {
        is EventoState.Success -> (eventoState as EventoState.Success).eventos
        else -> emptyList()
    }

    // User role
    val userRole by authViewModel.userRole.collectAsState()

    // color único para el fondo
    val bg = Color(0xFFF5F5F5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
    ) {
        // Fila con menú y título
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Spacer(modifier = Modifier.width(42.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material.Text(
                    text = "SQUADRA",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }

            // Icono de menú
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(end = 8.dp)
            ) {
                androidx.compose.material.IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterStart)
                ) {
                    androidx.compose.material.Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menú",
                        tint = primaryColor,
                        modifier = Modifier.size(32.dp)
                    )
                }


                androidx.compose.material.DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(Color.White)
                        .width(180.dp)
                ) {
                    androidx.compose.material.DropdownMenuItem(onClick = {
                        menuExpanded = false
                        onNavigateToMain()
                    }) {
                        androidx.compose.material.Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            tint = primaryColor
                        )
                        Spacer(Modifier.width(8.dp))
                        androidx.compose.material.Text("Inicio")
                    }

                    androidx.compose.material.DropdownMenuItem(onClick = {
                        menuExpanded = false
                        onNavigateToVerEntrenamientosEquipo()
                    }) {
                        androidx.compose.material.Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = primaryColor
                        )
                        Spacer(Modifier.width(8.dp))
                        androidx.compose.material.Text("Entrenamientos")
                    }

                    androidx.compose.material.DropdownMenuItem(onClick = {
                        menuExpanded = false
                        onNavigateToVerPartidosEquipo()
                    }) {
                        androidx.compose.material.Icon(
                            Icons.Default.SportsSoccer,
                            contentDescription = null,
                            tint = primaryColor
                        )
                        Spacer(Modifier.width(8.dp))
                        androidx.compose.material.Text("Partidos")
                    }

                    androidx.compose.material.DropdownMenuItem(onClick = {
                        menuExpanded = false
                        onProfile()
                    }) {
                        androidx.compose.material.Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = primaryColor
                        )
                        Spacer(Modifier.width(8.dp))
                        androidx.compose.material.Text("Perfil")
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = "Equipo: $nombreEquipo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = secondaryColor
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF9FAFB))
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Contenedor del calendario (2/3 de la pantalla)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f)
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    CalendarioEquipo(
                        currentMonth = currentMonth,
                        onChangeMonth = { newMonth -> calendarioViewModel.setMesSeleccionado(newMonth) },
                        onDayClick = { date ->
                            selectedDate = date
                            showEventDialog = true },
                        eventos = eventosMes
                    )

                }

                Spacer(modifier = Modifier.height(16.dp))

            }

            // Sección inferior con Divider y botones agrupados
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter) // Esta columna se coloca al fondo
            ) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onEstadisticaSelect) {
                        Text("Estadísticas", fontWeight = FontWeight.SemiBold, color = Color(0xFF263238))
                    }
                    TextButton(onClick = onJugadoresSelect) {
                        Text("Jugadores", fontWeight = FontWeight.SemiBold, color = Color(0xFF263238))
                    }
                }
            }
        }

    }

    // Después de calcular eventosMes y selectedDate:
    val eventosDelDia = selectedDate
        ?.let { fecha -> eventosMes.filter { it.fecha == fecha.toKotlinLocalDate() } }
        .orEmpty()

    // Dialogo para elegir tipo de evento
    if (showEventDialog && selectedDate != null) {
        if (eventosDelDia.isEmpty()) {
            if (userRole == "entrenador") {
                // ------------ DÍALOGO "CREAR" --------------
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showEventDialog = false },
                    confirmButton = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            TextButton(onClick = { showEventDialog = false }) {
                                Text("Cancelar", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    title = {
                        Text("Crear evento", style = MaterialTheme.typography.titleLarge)
                    },
                    text = {
                        Column {
                            Text(
                                "¿Qué deseas crear para el día ${selectedDate.toString()}?",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TextButton(onClick = {
                                    showEventDialog = false

                                    // Navegar a la pantalla de crear entrenamiento
                                    navController.navigate(Routes.CrearEntrenamiento.createRoute(idEquipo, selectedDate!!.toKotlinLocalDate(), idTemporada)) {
                                        launchSingleTop = true
                                        popUpTo(Routes.Equipo.route) { saveState = true }
                                        restoreState = true
                                    }
                                }) {
                                    Text("Crear Entrenamiento", color = MaterialTheme.colorScheme.primary)
                                }
                                TextButton(onClick = {
                                    showEventDialog = false

                                    // Navegar a la pantalla de crear partido
                                    navController.navigate(Routes.CrearPartido.createRoute(idEquipo, selectedDate!!.toKotlinLocalDate(), idTemporada)) {
                                        launchSingleTop = true
                                        popUpTo(Routes.Equipo.route) { saveState = true }
                                        restoreState = true
                                    }
                                }) {
                                    Text("Crear Partido", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                )
            } else {
                // si es coordinador, solo mostrar un mensaje
                AlertDialog(
                    onDismissRequest = { showEventDialog = false },
                    title = { Text("Acción no permitida") },
                    text = { Text("Los coordinadores solo pueden ver el calendario. Solo los entrenadores pueden crear eventos.") },
                    confirmButton = {
                        TextButton(onClick = { showEventDialog = false }) {
                            Text("Entendido")
                        }
                    }
                )
            }
        } else {
            if (userRole == "entrenador") {
                // —————— DÍALOGO “VER/MODIFICAR” ——————
                // Prepara el texto del título según los eventos del día
                val titleText = when {
                    eventosDelDia.all { it is EventoCalendario.Partido } ->
                        "Partido en $selectedDate"
                    eventosDelDia.all { it is EventoCalendario.Entrenamiento } ->
                        "Entrenamiento en $selectedDate"
                    else ->
                        "Eventos en $selectedDate"
                }
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showEventDialog = false },

                    // ------------ botón CERRAR abajo a la derecha ------------
                    confirmButton = {
                        TextButton(onClick = { showEventDialog = false }) {
                            Text("Cancelar", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {},

                    title = {
                        Text(titleText, style = MaterialTheme.typography.titleLarge)
                    },

                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            eventosDelDia.forEach { evento ->
                                when (evento) {
                                    is EventoCalendario.Entrenamiento -> {
                                        // — botón Ver entrenamiento arriba-izquierda —
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Text(
                                                "Ver entrenamiento",
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.clickable {
                                                    showEventDialog = false
                                                    calendarioViewModel.seleccionarEntrenamiento(evento)
                                                    // Navegar a la pantalla de ver entrenamiento
                                                    navController.navigate(Routes.VerEntrenamiento.route) {
                                                        launchSingleTop = true
                                                        popUpTo(Routes.Equipo.route) { saveState = true }
                                                        restoreState = true
                                                    }
                                                }
                                            )

                                            Text(
                                                "Añadir asistencia",
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.clickable {
                                                    showEventDialog = false
                                                    calendarioViewModel.seleccionarEntrenamiento(evento)
                                                    // Navegar a la pantalla de ver entrenamiento
                                                    navController.navigate(Routes.AsistenciaEntrenamiento.createRoute(evento.idEntrenamiento, Uri.encode(nombreEquipo))) {
                                                        launchSingleTop = true
                                                        popUpTo(Routes.Equipo.route) { saveState = true }
                                                        restoreState = true
                                                    }
                                                }
                                            )
                                        }

                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(
                                                "Eliminar entrenamiento",
                                                color = Color(0xFFD03605),
                                                modifier = Modifier
                                                    .clickable {
                                                        showEventDialog = false

                                                        //Obtener el mes actual.
                                                        val month = evento.fecha.month
                                                        val year = evento.fecha.year

                                                        val selectMonth = YearMonth(year, month)

                                                        if (!evento.entrenamientoUrl.isNullOrEmpty()) {
                                                            eliminarImagenDeFirebase(evento.entrenamientoUrl!!) { imagenEliminada ->
                                                                println("¿Imagen eliminada? $imagenEliminada")
                                                                calendarioViewModel.eliminarEntrenamiento(
                                                                    evento.idEntrenamiento,
                                                                    evento.idEquipo,
                                                                    selectMonth
                                                                )
                                                            }
                                                        } else {
                                                            calendarioViewModel.eliminarEntrenamiento(
                                                                evento.idEntrenamiento,
                                                                evento.idEquipo,
                                                                selectMonth
                                                            )
                                                        }
                                                    }
                                            )
                                        }
                                    }
                                    is EventoCalendario.Partido -> {
                                        // Fila 1: Botones Ver y Modificar partido
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "Ver partido",
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.clickable {
                                                    showEventDialog = false
                                                    calendarioViewModel.seleccionarPartido(evento)
                                                    navController.navigate(Routes.VerPartidoTabs.createRoute(idEquipo)) {
                                                        launchSingleTop = true
                                                        popUpTo(Routes.Equipo.route) { saveState = true }
                                                        restoreState = true
                                                    }
                                                }
                                            )
                                            Text(
                                                "Modificar partido",
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.clickable {
                                                    showEventDialog = false
                                                    calendarioViewModel.seleccionarPartido(evento)
                                                    // Navegar a la pantalla de ver entrenamiento
                                                    /*navController.navigate(Routes.ActualizarPartido.createRoute(evento.idEquipo, evento.idPartido, idTemporada, selectedDate!!.toKotlinLocalDate())) {
                                                        launchSingleTop = true
                                                        popUpTo(Routes.Equipo.route) { saveState = true }
                                                        restoreState = true
                                                    }*/
                                                    navController.navigate(Routes.ModificarPartidoTabs.createRoute(evento.idEquipo, evento.idPartido, idTemporada, selectedDate!!.toKotlinLocalDate(),  nombreEquipo)) {
                                                        launchSingleTop = true
                                                        popUpTo(Routes.Equipo.route) { saveState = true }
                                                        restoreState = true
                                                    }
                                                }
                                            )
                                        }

                                        // Fila 2: Botón Eliminar partido
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(
                                                "Eliminar partido",
                                                color = Color(0xFFD03605),
                                                modifier = Modifier.clickable {
                                                    // Lógica para eliminar el partido
                                                    showEventDialog = false

                                                    //Obtener el mes actual.
                                                    val month = evento.fecha.month
                                                    val year = evento.fecha.year

                                                    val selectMonth = YearMonth(year, month)

                                                    calendarioViewModel.eliminarPartido(
                                                        evento.idPartido,
                                                        evento.idEquipo,
                                                        selectMonth
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            } else {
                // —————— DÍALOGO “VER” ——————
                val titleText = when {
                    eventosDelDia.all { it is EventoCalendario.Partido } ->
                        "Partido en $selectedDate"
                    eventosDelDia.all { it is EventoCalendario.Entrenamiento } ->
                        "Entrenamiento en $selectedDate"
                    else ->
                        "Eventos en $selectedDate"
                }

                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showEventDialog = false },

                    // Botones abajo: Ver a la izquierda, Cancelar a la derecha
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Botón VER (izquierda)
                            if (eventosDelDia.size == 1) {
                                when (val evento = eventosDelDia.first()) {
                                    is EventoCalendario.Entrenamiento -> {
                                        TextButton(
                                            onClick = {
                                                showEventDialog = false
                                                calendarioViewModel.seleccionarEntrenamiento(evento)
                                                navController.navigate(Routes.VerEntrenamiento.route) {
                                                    launchSingleTop = true
                                                }
                                            }
                                        ) {
                                            Text("Ver entrenamiento", color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    is EventoCalendario.Partido -> {
                                        TextButton(
                                            onClick = {
                                                showEventDialog = false
                                                calendarioViewModel.seleccionarPartido(evento)
                                                navController.navigate(Routes.VerPartidoTabs.createRoute(idEquipo)) {
                                                    launchSingleTop = true
                                                    popUpTo(Routes.Equipo.route) { saveState = true }
                                                    restoreState = true
                                                }
                                            }
                                        ) {
                                            Text("Ver partido", color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                            // Botón CANCELAR (derecha)
                            TextButton(
                                onClick = { showEventDialog = false }
                            ) {
                                Text("Cancelar", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    dismissButton = {},

                    title = {
                        Text(titleText, style = MaterialTheme.typography.titleLarge)
                    },

                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Aquí puedes poner detalles extra si quieres,
                            // pero no más botones
                            Text("Selecciona una acción...")
                        }
                    }
                )
            }
        }
    }
}