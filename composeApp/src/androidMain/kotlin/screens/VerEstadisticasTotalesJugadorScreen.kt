package screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import network.EstadisticasTotalesJugador
import viewModel.AndroidEstadisticasViewModel
import viewModel.AndroidJugadorViewModel
import viewModel.EstadisticaTotalJugadorState

//private val AzulGrisaceo = Color(0xFF37474F)
//private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@Composable
fun VerEstadisticasTotalesJugadorScreen(
    idJugador: Int,
    idTemporada: Int,
    nombreEquipo: String,
    estadisticasViewModel: AndroidEstadisticasViewModel,
    jugadorViewModel: AndroidJugadorViewModel,
    onNavigateToDetalle: (String, Int) -> Unit,    // Recibe el nombre de la estadística para navegar
    onBack: () -> Unit
) {
    val estadisticaTotalJugadorState by estadisticasViewModel.estadisticasTotalJugadorState.collectAsState()
    val jugador by jugadorViewModel.jugadorSeleccionado.collectAsState()

    LaunchedEffect(idJugador, idTemporada) {
        estadisticasViewModel.getEstadisticasTotalesJugador(idJugador, idTemporada)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor) // Color de fondo claro
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SQUADRA",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Equipo: $nombreEquipo",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = secondaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Jugador: ${jugador?.nombreJugador}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = secondaryColor
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Estadísticas Totales",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = secondaryColor,
                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (estadisticaTotalJugadorState) {
                is EstadisticaTotalJugadorState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is EstadisticaTotalJugadorState.Error -> {
                    val msg = (estadisticaTotalJugadorState as EstadisticaTotalJugadorState.Error).message
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(msg, color = Color.Red)
                    }
                }
                is EstadisticaTotalJugadorState.Success -> {
                    val stats = (estadisticaTotalJugadorState as EstadisticaTotalJugadorState.Success).data


                    val datos = listOf(
                        "Goles" to stats.goles,
                        "Asistencias" to stats.asistencias,
                        "Min. Jugados" to stats.minutosJugados,
                        "Partidos Jugados" to stats.partidosJugados,
                        "Titularidades" to stats.partidosComoTitular,
                        "Tarjetas Amarillas" to stats.tarjetasAmarillas,
                        "Tarjetas Rojas" to stats.tarjetasRojas
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(datos) { (nombre, valor) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { jugador?.let { onNavigateToDetalle(nombre, it.idEquipo) } },
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = nombre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = secondaryColor
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = valor.toString(),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = secondaryColor
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