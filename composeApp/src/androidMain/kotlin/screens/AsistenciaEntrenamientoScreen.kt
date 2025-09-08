package screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import viewModel.AndroidAsistenciaEntrenamientoViewModel
import viewModel.AsistenciaEntrenamientoState
import viewModel.GuardadoState

// Colores de la app
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)
private val CardColor = Color.White

@Composable
fun AsistenciaEntrenamientoScreen(
    idEntrenamiento: Int,
    nombreEquipo: String,
    asistenciaEntrenamientoViewModel: AndroidAsistenciaEntrenamientoViewModel,
    onBack: () -> Unit
) {
    val state by asistenciaEntrenamientoViewModel.asistenciasState.collectAsState()
    val guardadoState by asistenciaEntrenamientoViewModel.guardadoState.collectAsState()


    LaunchedEffect(idEntrenamiento) {
        asistenciaEntrenamientoViewModel.loadAsistencias(idEntrenamiento)
    }

    Scaffold(
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(WindowInsets.safeDrawing.asPaddingValues()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SQUADRA",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Equipo: $nombreEquipo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF455A64)
                )
            }
        },
        bottomBar = {
            Button(
                onClick = { asistenciaEntrenamientoViewModel.saveAsistencias(onBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp), // altura fija para no "bailar"
                    contentAlignment = Alignment.Center
                ) {
                    when (guardadoState) {
                        is GuardadoState.Saving -> {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center),
                                color = Color.White,
                                trackColor = secondaryColor.copy(alpha = 0.3f)
                            )
                            Text(
                                "Guardando...",
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        is GuardadoState.Success -> {
                            Text("✔ Guardado", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                        is GuardadoState.Error -> {
                            Text("Error. Reintentar", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                        GuardadoState.Idle -> {
                            Text("Guardar Asistencias", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is AsistenciaEntrenamientoState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = secondaryColor)
                    }
                }
                is AsistenciaEntrenamientoState.Error -> {
                    val msg = (state as AsistenciaEntrenamientoState.Error).message
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(msg, color = MaterialTheme.colorScheme.error)
                    }
                }
                is AsistenciaEntrenamientoState.Success -> {
                    val lista = (state as AsistenciaEntrenamientoState.Success).asistencias

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(lista, key = { it.idJugador }) { dto ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(),
                                colors = CardDefaults.cardColors(containerColor = CardColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dto.nombreJugador,
                                            modifier = Modifier.weight(1f),
                                            fontSize = 16.sp,
                                            color = secondaryColor
                                        )
                                        Switch(
                                            checked = dto.asistio,
                                            onCheckedChange = { nuevoValor ->
                                                asistenciaEntrenamientoViewModel.toggleAsistencia(dto.idJugador, nuevoValor)
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = secondaryColor,
                                                checkedTrackColor = secondaryColor.copy(alpha = 0.5f),
                                                uncheckedThumbColor = Color.LightGray,
                                                uncheckedTrackColor = Color.Gray
                                            )
                                        )
                                    }

                                    // Mostrar motivo si no asistió
                                    if (!dto.asistio) {
                                        val motivo = remember { mutableStateOf(dto.motivoInasistencia ?: "") }

                                        OutlinedTextField(
                                            value = motivo.value,
                                            onValueChange = {
                                                motivo.value = it
                                                asistenciaEntrenamientoViewModel.actualizarMotivo(dto.idJugador, it)
                                            },
                                            label = { Text("Motivo de inasistencia") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
