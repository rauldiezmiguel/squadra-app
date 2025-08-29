package screens

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.IntOffset
import org.rauldiezmiguel.tfgfutbolbase.R
import viewModel.*


//private val AzulGrisaceo = Color(0xFF546E7A)
//private val FondoColor = Color(0xFFF5F5F5)
private val backgroundColor = Color(0xFFF9FAFB)
private val primaryColor = Color(0xFF263238)
private val secondaryColor = Color(0xFF455A64)

@SuppressLint("RememberReturnType")
@Composable
fun VerAnalisisEquipoPartidoScreen(
    idEquipo: Int,
    idPartido: Int,
    jugadorViewModel: AndroidJugadorViewModel,
    cuartosEquipoViewModel: AndroidCuartosEquipoViewModel,
    alineacionEquipoViewModel: AndroidAlineacionEquipoViewModel
) {
    val cuartoEquipoState by cuartosEquipoViewModel.cuartoEquipoState.collectAsState()
    val jugadorState by jugadorViewModel.jugadorState.collectAsState()
    val alineacionesPorCuarto by alineacionEquipoViewModel.alineacionesPorCuarto.collectAsState()
    val alineacionesMap = remember { mutableStateMapOf<Int, SnapshotStateMap<Int, JugadorConPosicion>>() }

    LaunchedEffect(Unit) {
        cuartosEquipoViewModel.obtenerCuartosEquipoPorPartido(idPartido)
        jugadorViewModel.getJugadoresPorEquipo(idEquipo)
    }

    LaunchedEffect(jugadorState) {
        if (jugadorState is JugadorState.Success) {
            val jugadores = (jugadorState as JugadorState.Success).jugadores.sortedBy { it.dorsal }
            alineacionEquipoViewModel.actualizarJugadores(jugadores)
        }
    }

    val cuartos = if (cuartoEquipoState is CuartoEquipoState.Success) {
        (cuartoEquipoState as CuartoEquipoState.Success).cuarto
    } else emptyList()

    val cuartosOrdenados = cuartos.sortedBy { it.numero }

    LaunchedEffect(cuartosOrdenados) {
        cuartosOrdenados.forEach { cuarto ->
            alineacionEquipoViewModel.obtenerAlineaciones(cuarto.id)
        }
    }

// Construimos la lista de jugadores con posición, agrupados por cuarto
    val jugadoresList = (jugadorState as? JugadorState.Success)?.jugadores ?: emptyList()

    val alineacionesUI = remember(cuartos, alineacionesPorCuarto, jugadorState) {
        alineacionesPorCuarto.mapValues { (_, alineacionesList) ->
            alineacionesList.mapNotNull { alineacion ->
                jugadoresList.find { it.id == alineacion.idJugador }?.let { jugador ->
                    JugadorConPosicion(
                        jugador = jugador,
                        offset = Offset(alineacion.posX, alineacion.posY),
                        idAlineacion = alineacion.id,
                        idCuarto = alineacion.idCuarto
                    )
                }
            }
        }.flatMap { it.value } // Flatten para tener una lista simple de JugadorConPosicion
    }

// Actualizamos el mapa local solo cuando cambia el número de alineaciones totales
    LaunchedEffect(alineacionesUI.size) {
        println("Alineaciones recibidas: ${alineacionesUI.size}")
        alineacionesUI.groupBy { it.idCuarto }.forEach { (idCuarto, alineaciones) ->
            val jugadoresDeCuarto = alineacionesMap.getOrPut(idCuarto) { mutableStateMapOf() }
            alineaciones.forEach { alineacion ->
                jugadoresDeCuarto[alineacion.idAlineacion] = alineacion
            }
            // Eliminamos alineaciones que ya no están
            jugadoresDeCuarto.keys.toList().forEach { idAlineacion ->
                if (alineaciones.none { it.idAlineacion == idAlineacion }) {
                    jugadoresDeCuarto.remove(idAlineacion)
                }
            }
        }
    }


    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        when (cuartoEquipoState) {
            is CuartoEquipoState.Success -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    cuartosOrdenados.forEachIndexed { index, cuarto ->
                        Text(
                            "Parte ${index + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = secondaryColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        val jugadoresDeCuarto = alineacionesMap.getOrPut(cuarto.id) { mutableStateMapOf() }

                        CampoConJugadoresSoloVista(
                            jugadores = jugadoresDeCuarto.values.toList()
                        )

                        Spacer(Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Funcionamiento",
                                    fontWeight = FontWeight.SemiBold,
                                    color = secondaryColor,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = cuarto.funcionamiento.orEmpty().ifBlank { "Sin información" },
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Daño Rival",
                                    fontWeight = FontWeight.SemiBold,
                                    color = secondaryColor,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = cuarto.danoRival.orEmpty().ifBlank { "Sin información" },
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Observaciones",
                                    fontWeight = FontWeight.SemiBold,
                                    color = secondaryColor,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = cuarto.observaciones.orEmpty().ifBlank { "Sin información" },
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
            is CuartoEquipoState.Loading -> CircularProgressIndicator()
            is CuartoEquipoState.Error -> Text("Error: ${(cuartoEquipoState as CuartoEquipoState.Error).message}")
            else -> {}
        }
    }
}

@Composable
fun CampoConJugadoresSoloVista(
    jugadores: List<JugadorConPosicion>
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
    ) {
        val campoWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val campoHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

        Image(
            painter = painterResource(id = R.drawable.campo_futbol_girado),
            contentDescription = "Campo de fútbol",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        jugadores.forEach { jugador ->
            val posX = jugador.offset.x.coerceIn(0f, 100f)
            val posY = jugador.offset.y.coerceIn(0f, 100f)

            val xPx = (posX / 100f) * campoWidthPx
            val yPx = (posY / 100f) * campoHeightPx

            Box(
                modifier = Modifier.offset {
                    IntOffset(xPx.toInt(), yPx.toInt())
                }
            ) {
                CamisetaJugador(jugador.jugador)
            }
        }
    }
}

