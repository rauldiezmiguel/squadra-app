package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.EventoCalendario
import network.PartidoDTO
import viewModel.AndroidCalendarioViewModel
import viewModel.AndroidEquipoViewModel
import viewModel.EquipoState
import viewModel.EventoState

//private val AzulGrisaceo = Color(0xFF546E7A)
//private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@Composable
fun VerListaPartidosScreen(
    viewModel: AndroidCalendarioViewModel,
    equipoViewModel: AndroidEquipoViewModel,
    onPartidoSelect: (EventoCalendario) -> Unit
) {
    val eventos by viewModel.eventoState.collectAsState()
    val equipos by equipoViewModel.equipoState.collectAsState()

    var equipoSeleccionado by rememberSaveable { mutableStateOf<Int?>(null) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var filtroAplicado by rememberSaveable { mutableStateOf(false) }
    var mostrarFiltros by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        equipoViewModel.getEquiposForUser()
    }

    LaunchedEffect(equipoSeleccionado, filtroAplicado) {
        if (filtroAplicado && equipoSeleccionado != null) {
            viewModel.getEventosForUser(equipoSeleccionado!!)
        }
    }

    val equiposLista = (equipos as? EquipoState.Success)?.equipos ?: emptyList()

    val partidos = (eventos as? EventoState.Success)?.eventos
        ?.filterIsInstance<EventoCalendario.Partido>()
        ?.filter {
            it.fecha.toString().contains(searchText, ignoreCase = true) ||
            it.nombreRival.contains(searchText, ignoreCase = true)
        } ?: emptyList()

    val total = partidos.count { !it.resultado.isNullOrBlank() }
    val ganados = partidos.count { it.resultado == "Victoria" }
    val empatados = partidos.count { it.resultado == "Empate" }
    val perdidos = partidos.count { it.resultado == "Derrota" }
    val porcentajeVictorias = if (total > 0) (ganados * 100 / total) else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(WindowInsets.safeDrawing.asPaddingValues()) // 👈 protección para notch, barras
    ) {
        // Scroll principal del contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = "SQUADRA",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Subtítulo: Entrenamientos + botón de filtro
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Espacio a la izquierda del mismo tamaño que el botón de la derecha
                Spacer(modifier = Modifier.size(48.dp))

                // Título centrado con peso
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Partidos",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                        color = secondaryColor
                    )
                }

                // Botón de filtro a la derecha
                IconButton(
                    onClick = { mostrarFiltros = !mostrarFiltros },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (!mostrarFiltros) Icons.Default.Tune else Icons.Default.Close,
                        contentDescription = "Mostrar/ocultar filtros",
                        tint = secondaryColor
                    )
                }
            }

            if (mostrarFiltros) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = secondaryColor)
                        ) {
                            Text(equiposLista.find { it.id == equipoSeleccionado }?.nombreEquipo ?: "Seleccionar equipo")
                        }

                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            equiposLista.forEach { equipo ->
                                DropdownMenuItem(
                                    text = { Text(equipo.nombreEquipo) },
                                    onClick = {
                                        equipoSeleccionado = equipo.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Buscar por fecha (YYYY-MM-DD) o por rival") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = secondaryColor,
                            focusedLabelColor = secondaryColor
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { filtroAplicado = true },
                        enabled = equipoSeleccionado != null,
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                    ) {
                        Text("Aplicar filtros")
                    }
                }
            }

            // Lista de partidos
            when {
                equipoSeleccionado == null -> {
                    Text("Selecciona un equipo para ver los partidos.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
                filtroAplicado && partidos.isEmpty() -> {
                    Text("No hay partidos disponibles para este filtro.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 82.dp
                            )
                    ) {
                        items(partidos) { partido ->
                            val colorResultado = when (partido.resultado) {
                                "Victoria" -> Color(0xFF4CAF50)
                                "Derrota" -> Color(0xFFF44336)
                                "Empate" -> Color(0xFFFFC107)
                                else -> Color.Transparent
                            }

                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.seleccionarPartido(partido)
                                        onPartidoSelect(partido)
                                    },
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            partido.nombreRival,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = secondaryColor
                                        )
                                        if (colorResultado != Color.Transparent) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(colorResultado, shape = MaterialTheme.shapes.small)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Fecha: ${partido.fecha}")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Estadísticas fijas abajo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(backgroundColor)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(icon = Icons.Default.List, text = "$total")
            StatItem(icon = Icons.Default.Check, text = "$ganados", iconColor = Color(0xFF4CAF50))
            StatItem(icon = Icons.Default.Remove, text = "$empatados", iconColor = Color(0xFFFFC107))
            StatItem(icon = Icons.Default.Close, text = "$perdidos", iconColor = Color(0xFFF44336))
            StatItem(icon = Icons.Default.EmojiEvents, text = "$porcentajeVictorias%")
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, text: String, iconColor: Color = primaryColor) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = primaryColor)
    }
}

