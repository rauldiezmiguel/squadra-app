package screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import viewModel.AndroidAuthViewModel
import viewModel.AndroidEquipoViewModel
import viewModel.EquipoState

@Composable
fun MainScreen(
    equipoViewModel: AndroidEquipoViewModel,
    onTeamSelected: (Int, String, Int) -> Unit,
    navController: NavController,
    onProfile: () -> Unit,
    authViewModel: AndroidAuthViewModel,
    onNavigateToMain: () -> Unit,
    onNavigateToVerEntrenamientosEquipo: () -> Unit,
    onNavigateToVerPartidosEquipo: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val equipoState by equipoViewModel.equipoState.collectAsState()
    val isLoggedOut = authViewModel.isLoggedOut.collectAsState().value
    var isRefreshing by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFF263238)
    val secondaryColor = Color(0xFF455A64)
    val backgroundColor = Color(0xFFF9FAFB)

    LaunchedEffect(isLoggedOut) {
        equipoViewModel.getEquiposForUser()
    }

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

        Spacer(modifier = Modifier.height(24.dp))

        // Ahora el contenido que quieres centrar vertical y horizontalmente
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            when (equipoState) {
                is EquipoState.Loading -> {
                    CircularProgressIndicator(color = secondaryColor)
                }

                is EquipoState.Success -> {
                    val equipos = (equipoState as EquipoState.Success).equipos
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth(0.85f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(equipos) { equipo ->
                            Button(
                                onClick = {
                                    onTeamSelected(equipo.id, equipo.nombreEquipo, equipo.idTemporada)
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = secondaryColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(equipo.nombreEquipo, color = Color.White, fontSize = 16.sp)
                            }
                        }
                    }
                }

                is EquipoState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (equipoState as EquipoState.Error).message,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        IconButton(
                            onClick = {
                                isRefreshing = true
                                equipoViewModel.getEquiposForUser()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(secondaryColor)
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

                else -> Unit
            }
        }
    }
}

@Composable
fun MenuItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}
