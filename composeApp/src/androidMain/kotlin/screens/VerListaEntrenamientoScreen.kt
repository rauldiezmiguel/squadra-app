package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.EventoCalendario
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
fun VerListaEntrenamientoScreen(
    viewModel: AndroidCalendarioViewModel,
    equipoViewModel: AndroidEquipoViewModel,
    onEntrenamientoSelect: () -> Unit
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

    val entrenamientos = (eventos as? EventoState.Success)?.eventos
        ?.filterIsInstance<EventoCalendario.Entrenamiento>()
        ?.filter {
            it.fecha.toString().contains(searchText, ignoreCase = true)
        } ?: emptyList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
    ) {

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
                        text = "Entrenamientos",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                        color = primaryColor
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
                        tint = secondaryColor,
                        modifier = Modifier.size(32.dp)
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
                        label = { Text("Buscar por fecha (YYYY-MM-DD)") },
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

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            when {
                equipoSeleccionado == null -> {
                    Text("Selecciona un equipo para ver los entrenamientos.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
                filtroAplicado && entrenamientos.isEmpty() -> {
                    Text("No hay entrenamientos disponibles para este filtro.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(entrenamientos) { entrenamiento ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.seleccionarEntrenamiento(entrenamiento)
                                        onEntrenamientoSelect()
                                    },
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Entrenamiento",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = primaryColor
                                    )
                                    Text("Fecha: ${entrenamiento.fecha}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


