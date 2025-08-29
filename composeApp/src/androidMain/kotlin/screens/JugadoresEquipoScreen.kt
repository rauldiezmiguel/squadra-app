package screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.JugadorDTO
import viewModel.AndroidAuthViewModel
import viewModel.AndroidJugadorViewModel
import viewModel.JugadorState

@Composable
fun JugadoresEquipoScreen(
    onProfile: () -> Unit,
    idEquipo: Int,
    nombreEquipo: String,
    onJugadorSelected: (JugadorDTO) -> Unit,
    onCreateJugador: (Int) -> Unit,
    jugadorViewModel: AndroidJugadorViewModel,
    authViewModel: AndroidAuthViewModel,
    onNavigateToMain: () -> Unit,
    onNavigateToVerEntrenamientosEquipo: () -> Unit,
    onNavigateToVerPartidosEquipo: () -> Unit,
    onVerFichaJugadores: () -> Unit
) {
    val jugadorState by jugadorViewModel.jugadorState.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()

    var menuExpanded by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Cargar jugadores al entrar
    LaunchedEffect(idEquipo) {
        jugadorViewModel.getJugadoresPorEquipo(idEquipo)
    }

    val backgroundColor = Color(0xFFF9FAFB)
    val primaryColor = Color(0xFF263238)
    val secondaryColor = Color(0xFF455A64)
    val accentColor = Color(0xFF7CB342)

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
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menú",
                        tint = primaryColor,
                        modifier = Modifier.size(32.dp)
                    )
                }


                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(Color.White)
                        .width(180.dp)
                ) {
                    DropdownMenuItem(onClick = {
                        menuExpanded = false
                        onNavigateToMain()
                    }) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = primaryColor)
                        Spacer(Modifier.width(8.dp))
                        Text("Inicio")
                    }

                    DropdownMenuItem(onClick = {
                        menuExpanded = false
                        onNavigateToVerEntrenamientosEquipo()
                    }) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = primaryColor)
                        Spacer(Modifier.width(8.dp))
                        Text("Entrenamientos")
                    }

                    DropdownMenuItem(onClick = {
                        menuExpanded = false
                        onNavigateToVerPartidosEquipo()
                    }) {
                        Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = primaryColor)
                        Spacer(Modifier.width(8.dp))
                        Text("Partidos")
                    }

                    DropdownMenuItem(onClick = {
                        menuExpanded = false
                        onProfile()
                    }) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor)
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            when (jugadorState) {
                is JugadorState.Loading -> {
                    androidx.compose.material3.CircularProgressIndicator()
                }
                is JugadorState.Success -> {
                    val jugadores = (jugadorState as JugadorState.Success).jugadores.sortedBy { it.dorsal }
                    if (jugadores.isEmpty()) {
                        Text("No hay jugadores en este equipo.")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(jugadores) { jugador ->
                                JugadorButton(jugador = jugador, onClick = onJugadorSelected)
                            }
                        }
                    }
                }
                is JugadorState.Error -> {
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                (jugadorState as JugadorState.Error).message,
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            IconButton(
                                onClick = {
                                    isRefreshing = true
                                    jugadorViewModel.getJugadoresPorEquipo(idEquipo)
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF546E7A), shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Recargar",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
                else -> {}
            }

            // Solo si no eres coordinador, mostrar botón de añadir jugador
            if (!userRole.equals("coordinador", ignoreCase = true)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp), // Añadir algo de espacio arriba si es necesario
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre los botones
                ) {
                    Button(
                        onClick = { onCreateJugador(idEquipo) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(accentColor)
                    ) {
                        Text("Añadir Jugador", color = Color.White)
                    }
                    Button(
                        onClick = { onVerFichaJugadores() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = secondaryColor)
                    ) {
                        Text("Ver Fichas", color = Color.White)
                    }
                }
            } else {
                Button(
                    onClick = { onVerFichaJugadores() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = secondaryColor)
                ) {
                    Text("Ver fichas jugador", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun JugadorButton(jugador: JugadorDTO, onClick: (JugadorDTO) -> Unit) {
    val buttonColor = Color(0xFF455A64) // secondaryColor
    val textColor = Color.White

    Button(
        onClick = { onClick(jugador) },
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 6.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = buttonColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        elevation = null // quita la sombra para estilo más plano
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = jugador.nombreJugador,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Text(
                    text = "Dorsal: ${jugador.dorsal}",
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver jugador",
                tint = textColor.copy(alpha = 0.5f)
            )
        }
    }
}


