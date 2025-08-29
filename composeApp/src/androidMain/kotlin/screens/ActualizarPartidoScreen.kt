package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import network.ActualizarResultadoPartidoRequest
import network.CrearEstadisticasRequest
import viewModel.AndroidJugadorViewModel
import viewModel.JugadorState


//private val AzulGrisaceo = Color(0xFF546E7A)
//private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActualizarPartidoScreen(
    idEquipo: Int,
    idPartido: Int,
    jugadorViewModel: AndroidJugadorViewModel,
    onDismiss: () -> Unit,
    onUpdatePartido: (ActualizarResultadoPartidoRequest, (Boolean, String?) -> Unit) -> Unit,
    onCreateEstadistica: (CrearEstadisticasRequest) -> Unit
) {
    val jugadorState by jugadorViewModel.jugadorState.collectAsState()

    LaunchedEffect(Unit) {
        jugadorViewModel.getJugadoresPorEquipo(idEquipo)
    }

    var resultadoNumerico by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var estadisticaSeleccionada by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val opcionesResultado = listOf("Victoria", "Empate", "Derrota")

    val jugadores = when (jugadorState) {
        is JugadorState.Success -> (jugadorState as JugadorState.Success).jugadores.sortedBy { it.dorsal }
        else -> emptyList()
    }

    // ✅ Versión reactiva del mapa
    val estadisticasPorJugador = remember(jugadores) {
        mutableStateMapOf<Int, MutableMap<String, MutableState<Any>>>().apply {
            jugadores.forEach { jugador ->
                put(jugador.id, mutableMapOf(
                    "Minutos Jugados" to mutableStateOf(0),
                    "Goles" to mutableStateOf(0),
                    "Asistencias" to mutableStateOf(0),
                    "Partido Jugado" to mutableStateOf(true),
                    "Titular" to mutableStateOf(false),
                    "Tarjetas Amarillas" to mutableStateOf(0),
                    "Tarjetas Rojas" to mutableStateOf(0)
                ))
            }
        }
    }

    val estadisticasName = listOf(
        "Minutos Jugados", "Goles", "Asistencias", "Partido Jugado",
        "Titular", "Tarjetas Amarillas", "Tarjetas Rojas"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /*
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Actualizar Partido",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )
         */

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = resultadoNumerico,
            onValueChange = { resultadoNumerico = it },
            label = { Text("Resultado numérico", color = secondaryColor) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                readOnly = true,
                value = resultado,
                onValueChange = {},
                label = { Text("Resultado", color = secondaryColor) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                opcionesResultado.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            resultado = opcion
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (jugadorState) {
            is JugadorState.Success -> {
                Text(
                    "Estadísticas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = secondaryColor,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(estadisticasName) { estadistica ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    estadisticaSeleccionada = estadistica
                                },
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(estadistica, style = MaterialTheme.typography.bodyMedium, color = secondaryColor)
                            }
                        }
                    }
                }

                if (estadisticaSeleccionada != null) {
                    AlertDialog(
                        onDismissRequest = { estadisticaSeleccionada = null },
                        confirmButton = {
                            Button(
                                onClick = { estadisticaSeleccionada = null },
                                colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                            ) {
                                Text("Aceptar", color = Color.White)
                            }
                        },
                        title = {
                            Text(
                                "Editar $estadisticaSeleccionada",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = secondaryColor
                            )
                        },
                        text = {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 400.dp, max = 600.dp),
                                shape = RoundedCornerShape(12.dp),
                                tonalElevation = 2.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(jugadores) { jugador ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .padding(12.dp)
                                                        .fillMaxWidth(),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    CamisetaJugador(
                                                        jugador = jugador,
                                                        size = 70.dp // Ajusta el tamaño según necesites
                                                    )

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    if (estadisticaSeleccionada in listOf("Partido Jugado", "Titular")) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Checkbox(
                                                                checked = estadisticasPorJugador[jugador.id]!![estadisticaSeleccionada]!!.value as Boolean,
                                                                onCheckedChange = {
                                                                    estadisticasPorJugador[jugador.id]!![estadisticaSeleccionada!!]!!.value = it
                                                                }
                                                            )
                                                            Text(
                                                                if (estadisticasPorJugador[jugador.id]!![estadisticaSeleccionada]!!.value as Boolean)
                                                                    "Sí" else "No",
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                        }
                                                    } else {
                                                        OutlinedTextField(
                                                            value = estadisticasPorJugador[jugador.id]!![estadisticaSeleccionada]!!.value.toString(),
                                                            onValueChange = {
                                                                estadisticasPorJugador[jugador.id]!![estadisticaSeleccionada!!]!!.value =
                                                                    it.toIntOrNull() ?: 0
                                                            },
                                                            label = { Text("Valor") },
                                                            singleLine = true,
                                                            modifier = Modifier.fillMaxWidth()
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = Color.White
                    )
                }

            }

            is JugadorState.Loading -> CircularProgressIndicator()
            is JugadorState.Error -> Text("Error: ${(jugadorState as JugadorState.Error).message}", color = Color.Red)
            else -> {}
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (jugadores.isNotEmpty()) {
            Button(
                onClick = {
                    jugadores.forEach { jugador ->
                        val estadisticas = estadisticasPorJugador[jugador.id]!!
                        val partidoJugado = estadisticas["Partido Jugado"]!!.value as Boolean
                        val titular = estadisticas["Titular"]!!.value as Boolean

                        val request = CrearEstadisticasRequest(
                            idJugador = jugador.id,
                            idPartido = idPartido,
                            minutosJugados = estadisticas["Minutos Jugados"]!!.value as Int,
                            goles = estadisticas["Goles"]!!.value as Int,
                            asistencias = estadisticas["Asistencias"]!!.value as Int,
                            partidoJugado = partidoJugado,
                            titular = if (partidoJugado) titular else null,
                            tarjetasAmarillas = estadisticas["Tarjetas Amarillas"]!!.value as Int,
                            tarjetasRojas = estadisticas["Tarjetas Rojas"]!!.value as Int
                        )
                        onCreateEstadistica(request)
                    }

                    isUpdating = true
                    val partidoRequest = ActualizarResultadoPartidoRequest(
                        resultadoNumerico = resultadoNumerico,
                        resultado = resultado
                    )
                    onUpdatePartido(partidoRequest) { success, error ->
                        isUpdating = false
                        //if (success) onDismiss() else errorMessage = error
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = !isUpdating && resultado.isNotEmpty() && resultadoNumerico.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
            ) {
                Text(if (isUpdating) "Actualizando..." else "Guardar", color = Color.White)
            }
        }

    }
}
