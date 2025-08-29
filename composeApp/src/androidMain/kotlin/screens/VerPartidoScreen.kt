package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.EventoCalendario
import network.JugadorDTO
import viewModel.AndroidEstadisticasViewModel
import viewModel.AndroidJugadorViewModel
import viewModel.EstadisticasJugadorPartidoState
import viewModel.JugadorState


//private val AzulGrisaceo = Color(0xFF546E7A)
//private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@Composable
fun VerPartidoScreen(
    partido: EventoCalendario.Partido,
    jugadorViewModel: AndroidJugadorViewModel,
    estadisticasViewModel: AndroidEstadisticasViewModel
) {
    var estadisticaSeleccionada by remember { mutableStateOf<String?>(null) }

    val estadisticasState by estadisticasViewModel.estadisticasJugadorPartido.collectAsState()

    val idEquipo = partido.idEquipo

    LaunchedEffect(idEquipo) {
        jugadorViewModel.getJugadoresPorEquipo(idEquipo)
    }

    val jugadorState by jugadorViewModel.jugadorState.collectAsState()
    val jugadores = when (jugadorState) {
        is JugadorState.Success -> (jugadorState as JugadorState.Success).jugadores
        else -> emptyList()
    }

    LaunchedEffect(jugadores) {
        estadisticasViewModel.getEstadisticasDeJugadoresParaPartido(jugadores, partido.idPartido)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp) // solo horizontal, deja el espacio superior al tabs
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))
        InfoCard(
            icon = Icons.Filled.SportsSoccer,
            title = "Rival",
            content = partido.nombreRival,
            titleColor = secondaryColor
        )

        InfoCard(
            icon = Icons.Filled.DateRange,
            title = "Fecha",
            content = partido.fecha.toString(),
            titleColor = secondaryColor
        )

        partido.resultadoNumerico
            ?.takeIf { it.isNotBlank() }
            ?.let {
                val borderColor = when (partido.resultado) {
                    "Victoria" -> Color(0xFF4CAF50)
                    "Derrota" -> Color(0xFFF44336)
                    "Empate" -> Color(0xFFFF9800)
                    else -> secondaryColor
                }

                InfoCard(
                    icon = Icons.Filled.CheckCircle,
                    title = "Resultado",
                    content = it,
                    titleColor = borderColor,
                    borderColor = borderColor
                )
            }

        Text(
            text = "Estadísticas",
            style = MaterialTheme.typography.titleMedium,
            color = secondaryColor
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .weight(1f)
        ) {
            items(
                listOf(
                    "Minutos Jugados", "Goles", "Asistencias",
                    "Partido Jugado", "Titular", "Tarjetas Amarillas", "Tarjetas Rojas"
                )
            ) { estadistica ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { estadisticaSeleccionada = estadistica },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            estadistica,
                            style = MaterialTheme.typography.titleSmall,
                            color = secondaryColor
                        )
                    }
                }
            }
        }

        estadisticaSeleccionada?.let { estadistica ->
            val jugadoresConEstadistica = if (estadisticasState is EstadisticasJugadorPartidoState.Succes) {
                (estadisticasState as EstadisticasJugadorPartidoState.Succes).data.filter { dto ->
                    when (estadistica) {
                        "Minutos Jugados" -> dto.minutosJugados > 0
                        "Goles" -> dto.goles > 0
                        "Asistencias" -> dto.asistencias > 0
                        "Partido Jugado" -> dto.partidoJugado
                        "Titular" -> dto.titular
                        "Tarjetas Amarillas" -> dto.tarjetasAmarillas > 0
                        "Tarjetas Rojas" -> dto.tarjetasRojas > 0
                        else -> false
                    }
                }.mapNotNull { dto ->
                    jugadores.find { it.id == dto.idJugador }?.let { jugador ->
                        jugador to dto
                    }
                }
            } else emptyList()

            AlertDialog(
                onDismissRequest = { estadisticaSeleccionada = null },
                confirmButton = {
                    Button(
                        onClick = { estadisticaSeleccionada = null },
                        colors = ButtonDefaults.buttonColors(containerColor = secondaryColor)
                    ) {
                        Text("Cerrar", color = Color.White)
                    }
                },
                title = {
                    if (estadistica == "Partido Jugado"){
                        Text(
                            "Partido jugado",
                            style = MaterialTheme.typography.titleMedium.copy(color = secondaryColor)
                        )
                    } else if (estadistica == "Titular") {
                        Text(
                            "Jugadores titulares",
                            style = MaterialTheme.typography.titleMedium.copy(color = secondaryColor)
                        )
                    } else {
                        Text(
                            "Jugadores con $estadistica",
                            style = MaterialTheme.typography.titleMedium.copy(color = secondaryColor)
                        )
                    }
                },
                text = {
                    if (jugadoresConEstadistica.isEmpty()) {
                        Text("Ningún jugador tiene $estadistica.", color = secondaryColor)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            jugadoresConEstadistica.forEach { (jugador, dto) ->
                                val valor = when (estadistica) {
                                    "Minutos Jugados" -> dto.minutosJugados.toString()
                                    "Goles" -> dto.goles.toString()
                                    "Asistencias" -> dto.asistencias.toString()
                                    "Partido Jugado" -> if (dto.partidoJugado) "Si" else "No"
                                    "Titular" -> if (dto.titular) "Si" else "No"
                                    "Tarjetas Amarillas" -> dto.tarjetasAmarillas.toString()
                                    "Tarjetas Rojas" -> dto.tarjetasRojas.toString()
                                    else -> "-"
                                }
                                if ((estadistica == "Partido Jugado") || (estadistica == "Titular")){
                                    Text(jugador.nombreJugador, color = secondaryColor)
                                } else {
                                    Text("${jugador.nombreJugador}: $valor", color = secondaryColor)
                                }
                            }
                        }
                    }
                },
                containerColor = Color.White
            )
        }
    }
}


@Composable
fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    titleColor: Color,
    borderColor: Color = secondaryColor // Por defecto secondaryColor
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 2.dp, color = borderColor, shape = MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = titleColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}