package screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import viewModel.AndroidAuthViewModel
import viewModel.AndroidJugadorViewModel

@Composable
fun JugadorScreen(
    idEquipo: Int,
    nombreEquipo: String,
    navController: NavController,
    jugadorViewModel: AndroidJugadorViewModel,
    authViewModel: AndroidAuthViewModel,
    onProfile: () -> Unit,
    onVerEstadisticas: (Int) -> Unit,
    onAnadirEstadisticas: (Int) -> Unit,
    onVerEvaluaciones: (Int) -> Unit,
    onAnadirEvaluaciones: (Int) -> Unit,
    onVerAsistenciaEntrenamientos: (Int) -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToVerEntrenamientosEquipo: () -> Unit,
    onNavigateToVerPartidosEquipo: () -> Unit,
    onCrearFichaJugador: (Int) -> Unit,
    onEditarJugador: (Int) -> Unit
) {
    val userRole by authViewModel.userRole.collectAsState()
    val isEntrenador = userRole == "entrenador"
    var menuExpanded by remember { mutableStateOf(false) }
    val jugador by jugadorViewModel.jugadorSeleccionado.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFFF9FAFB)
    val primaryColor = Color(0xFF263238)
    val secondaryColor = Color(0xFF455A64)
    val accentColor = Color(0xFF7CB342)

    Scaffold(
        modifier = Modifier.fillMaxSize().background(backgroundColor),
        topBar = {},
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
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
                    Text(
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
                            Text("Inicio")
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
                            Text("Entrenamientos")
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
                            Text("Partidos")
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
                            Text("Perfil")
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
                        color = Color(0xFF455A64)
                    )
                }
            }

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                jugador?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onEditarJugador(it.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Jugador", fontWeight = FontWeight.Bold, color = primaryColor)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Nombre: ${it.nombreJugador}", color = secondaryColor)
                                Text("Dorsal: ${it.dorsal}", color = secondaryColor)
                                Text("Posición: ${it.posicion}", color = secondaryColor)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Acciones",
                    fontSize = 18.sp,
                    color = Color(0xFF546E7A),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Scrollable solo para botones
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isEntrenador) {
                            StyledButton("Ver estadísticas", { onVerEstadisticas(idEquipo) }, secondaryColor, Color.White)
                            StyledButton("Ver evaluaciones", { onVerEvaluaciones(idEquipo) }, secondaryColor, Color.White)
                            StyledButton("Añadir evaluaciones", { onAnadirEvaluaciones(idEquipo) }, secondaryColor, Color.White)
                            StyledButton("Ver asistencia entrenamientos", { onVerAsistenciaEntrenamientos(idEquipo) }, secondaryColor, Color.White)
                            StyledButton("Añadir ficha jugador", { onCrearFichaJugador(idEquipo) }, secondaryColor, Color.White)
                        } else {
                            StyledButton("Ver estadísticas", { onVerEstadisticas(idEquipo) }, secondaryColor, Color.White)
                            StyledButton("Ver evaluaciones", { jugador?.let { onVerEvaluaciones(it.id) } }, secondaryColor, Color.White)
                            StyledButton("Ver asistencia entrenamientos", { onVerAsistenciaEntrenamientos(idEquipo) }, secondaryColor, Color.White)
                        }
                    }
                }

                if (isEntrenador) {
                    Spacer(modifier = Modifier.height(16.dp))
                    StyledButton(
                        text = "Eliminar jugador",
                        onClick = { showConfirmDialog = true },
                        containerColor = Color(0xFFEF5350),
                        contentColor = Color.White
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Eliminar jugador", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Text(
                        "¿Estás seguro de que deseas eliminar este jugador?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        jugador?.let {
                            jugadorViewModel.eliminarJugador(it.id, idEquipo)
                            showConfirmDialog = false
                            navController.popBackStack()
                        }
                    }) {
                        Text("Sí, eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    }
}

// Composable para botones estilizados
@Composable
fun StyledButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, containerColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = contentColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = containerColor
        )
    }
}

